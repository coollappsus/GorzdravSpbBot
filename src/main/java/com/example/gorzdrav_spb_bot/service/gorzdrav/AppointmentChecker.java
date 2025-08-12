package com.example.gorzdrav_spb_bot.service.gorzdrav;

import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
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
            allAppointments.stream()
                    .filter(a -> {
                        LocalTime visitLocalTime = a.visitStart().toInstant().atZone(zone).toLocalTime();
                        return switch (task.getTimePreference()) {
                            case EARLY -> visitLocalTime.isBefore(LocalTime.NOON);
                            case MID -> !visitLocalTime.isBefore(LocalTime.NOON)
                                    && visitLocalTime.isBefore(LocalTime.of(16, 0));
                            case LATE -> !visitLocalTime.isBefore(LocalTime.of(16, 0));
                        };
                    }).sorted(Comparator.comparing(Appointment::visitStart))
                    .findAny()
                    .ifPresentOrElse(
                            a -> {
                                appointment.set(a);
                                gorzdravService.createAppointment(a, lpuId, patientId);
                                log.info("The appointment was created for a preferred time, task = {}", task.getId());
                            },
                            () -> {
                                Appointment a = allAppointments.get(0);
                                appointment.set(a);
                                gorzdravService.createAppointment(a, lpuId, patientId);
                                log.info("Appointment was created for any free time, no preferred time was found, " +
                                        "task = {}", task.getId());
                            });
            task.doFinished();
            taskRepository.save(task);

            telegramAsyncMessageSender.sendMessageToUser(task.getOwner().getChatId(),
                    getMessageByAppointment(appointment.get(), task.getMedicalCard()));
        }
    }

    private String getMessageByAppointment(Appointment appointment, MedicalCard medicalCard) {
        return MESSAGE_TEXT.formatted(appointment.address(),
                FIRST_DATE_FORMAT.format(appointment.visitStart()) + " - " + SECOND_DATE_FORMAT.format(appointment.visitEnd()),
                medicalCard.getLastName() + " " + medicalCard.getFirstName() + " " + medicalCard.getMiddleName());
    }
}
