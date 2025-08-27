package com.example.gorzdrav_spb_bot.service.gorzdrav;

import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.Task;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
@Slf4j
public class AppointmentChecker {

    private static final String MESSAGE_TEXT = """
            ✨Поздравляю! Талончик найден и запись к врачу создана!
            🚑Лечебно-профилактическое учреждение по адресу %s
            ⏱Время - %s
            👤ФИО пациента - %s
            """;
    private static final String ERROR_MESSAGE_TEXT = """
            ❌Талончик был найден, но произошла ошибка во время записи к врачу.
            Ошибка на стороне Горздрава.
            Данная задача на отслеживание отменена во избежание спама.
            """;

    private static final SimpleDateFormat FIRST_DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm");
    private static final SimpleDateFormat SECOND_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private static final ZoneId zone = ZoneId.of("Europe/Moscow");

    private final TaskRepository taskRepository;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;
    private final GorzdravService gorzdravService;

    @Scheduled(fixedDelay = 30000) //Раз в 30 секунд
    public void checkAndCreateAppointment() {
        var tasks = taskRepository.findByCompleteStatusAndActiveStatus(false, true);

        for (var task : tasks) {
            log.info("Checking appointment for task {}", task.getId());
            String lpuId = task.getLpuId();
            var allAppointments = gorzdravService.getAppointments(lpuId, task.getDoctorId());

            if (allAppointments.isEmpty()) {
                log.info("No appointments found for task {}", task.getId());
                continue;
            }

            String patientId = task.getMedicalCard().getPatientId();
            AtomicReference<Appointment> appointment = new AtomicReference<>();
            List<Appointment> hardFilteredList = hardFiltering(allAppointments, task);
            if (hardFilteredList.isEmpty()) {
                log.info("Appointment not found for task {} on hard filter. Repeat search again later", task.getId());
                continue;
            }

            softFiltering(hardFilteredList, task)
                    .ifPresentOrElse(
                            appointment1 -> createAppointmentWithPreferenceTime(task, appointment1, appointment, lpuId,
                                    patientId),
                            () -> createAppointmentWithoutPreferenceTime(task, hardFilteredList, appointment, lpuId,
                                    patientId));
        }
    }

    private void createAppointmentWithoutPreferenceTime(Task task, List<Appointment> hardFilteredList,
                                                        AtomicReference<Appointment> appointment,
                                                        String lpuId, String patientId) {
        try {
            Appointment appointment1 = hardFilteredList.get(0);
            appointment.set(appointment1);
            gorzdravService.createAppointment(appointment1, lpuId, patientId);
            doCompleteTaskAndNotifyUser(task, appointment, false);
            log.info("Appointment was created for any free time, no preferred time was found, task = {}",
                    task.getId());
        } catch (ResponseStatusException e) {
            doCompleteTaskAndNotifyUser(task, appointment, true);
            log.error("Appointment was found, but gorzdrav response error in processing create appointment, task = {}",
                    task.getId());
        }
    }

    private void createAppointmentWithPreferenceTime(Task task, Appointment appointment1,
                                                     AtomicReference<Appointment> appointment,
                                                     String lpuId, String patientId) {
        try {
            appointment.set(appointment1);
            gorzdravService.createAppointment(appointment1, lpuId, patientId);
            doCompleteTaskAndNotifyUser(task, appointment, false);
            log.info("The appointment was created for appointment1 preferred time, task = {}",
                    task.getId());
        } catch (Exception e) {
            doCompleteTaskAndNotifyUser(task, appointment, true);
            log.error("Appointment was found, but gorzdrav response error in processing create appointment, task = {}",
                    task.getId());
        }
    }

    private void doCompleteTaskAndNotifyUser(Task task, AtomicReference<Appointment> appointment, boolean isError) {
        task.doFinished();
        taskRepository.save(task);

        if (isError) {
            telegramAsyncMessageSender.sendMessageToUser(task.getOwner().getChatId(),
                    getMessageByAppointment(appointment.get(), task.getMedicalCard()));
        } else {
            telegramAsyncMessageSender.sendMessageToUser(task.getOwner().getChatId(), ERROR_MESSAGE_TEXT);
        }
    }

    private String getMessageByAppointment(Appointment appointment, MedicalCard medicalCard) {
        return MESSAGE_TEXT.formatted(appointment.address(),
                FIRST_DATE_FORMAT.format(appointment.visitStart()) + " - " + SECOND_DATE_FORMAT.format(appointment.visitEnd()),
                medicalCard.getLastName() + " " + medicalCard.getFirstName() + " " + medicalCard.getMiddleName());
    }

    private Optional<Appointment> softFiltering(Collection<Appointment> allAppointments, Task task) {
        return allAppointments.stream()
                .filter(a -> {
                    LocalTime visitLocalTime = a.visitStart().toInstant().atZone(zone).toLocalTime();
                    return switch (task.getTimePreference()) {
                        case EARLY -> visitLocalTime.isBefore(LocalTime.NOON);
                        case MID -> !visitLocalTime.isBefore(LocalTime.NOON)
                                && visitLocalTime.isBefore(LocalTime.of(17, 0));
                        case LATE -> !visitLocalTime.isBefore(LocalTime.of(17, 0));
                    };
                }).sorted(Comparator.comparing(Appointment::visitStart))
                .findAny();
    }

    private List<Appointment> hardFiltering(Collection<Appointment> allAppointments, Task task) {
        return allAppointments.stream()
                .filter(a -> {
                    if (task.getPreferenceDate() != null) {
                        return DateUtils.isSameDay(task.getPreferenceDate(), a.visitStart());
                    } else {
                        return true;
                    }
                }).toList();
    }
}
