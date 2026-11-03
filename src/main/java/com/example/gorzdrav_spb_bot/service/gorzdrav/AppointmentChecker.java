package com.example.gorzdrav_spb_bot.service.gorzdrav;

import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.Task;
import com.example.gorzdrav_spb_bot.repository.DoctorDictRepositoryImpl;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class AppointmentChecker {

    private static final String MESSAGE_TEXT = """
            ✨Поздравляю! Талончик найден и запись к врачу создана!
            🚑Лечебно-профилактическое учреждение по адресу %s
            ⏱Время - %s
            👤ФИО пациента - %s
            🧷Задача №%s
            """;
    private static final String FULL_MESSAGE_TEXT = MESSAGE_TEXT + """
            \nФИО врача - %s
            Специальность - %s
            """;
    private static final String ERROR_MESSAGE_TEXT = """
            ❌Талончик был найден, но произошла ошибка во время записи к врачу.
            🧷Задача №%s
            Ошибка на стороне Горздрава, либо нарушены ограничения для записи к данному специалисту. Подробнее в ошибке.
            Данная задача на отслеживание отменена во избежание спама в ТГ.
            %s
            """;

    private static final SimpleDateFormat FIRST_DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));
    private static final SimpleDateFormat SECOND_DATE_FORMAT = new SimpleDateFormat("HH:mm", new Locale("ru"));
    private static final ZoneId zone = ZoneId.of("Europe/Moscow");

    private final TaskRepository taskRepository;
    private final DoctorDictRepositoryImpl doctorDictRepository;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;
    private final GorzdravService gorzdravService;
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Точка старта. Запускает цикл опроса сразу после готовности приложения.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startCheckingLoop() {
        scheduleNextCheck();
    }

    /**
     * Планирует следующий запуск на случайное время (от 20 до 40 секунд)
     */
    private void scheduleNextCheck() {
        long randomDelayMs = ThreadLocalRandom.current().nextLong(20000, 40001);
        log.debug("Следующий общий круг проверки запланирован через {} сек.", randomDelayMs / 1000.0);

        taskScheduler.schedule(
                this::runCheckAndScheduleNext,
                Instant.now().plusMillis(randomDelayMs)
        );
    }

    /**
     * Обёртка для безопасного выполнения и обязательного планирования следующего шага
     */
    private void runCheckAndScheduleNext() {
        try {
            processAllTasksDistributed();
        } catch (Exception e) {
            log.error("Критическая ошибка во время круга проверки талонов", e);
        } finally {
            scheduleNextCheck();
        }
    }

    /**
     * Главный метод распределения задач по больницам (ЛПУ)
     */
    private void processAllTasksDistributed() {
        var activeTasks = taskRepository.findByCompleteStatusAndActiveStatus(false, true);
        if (activeTasks.isEmpty()) {
            log.debug("Нет активных задач для отслеживания.");
            return;
        }

        Map<String, List<Task>> tasksByLpu = activeTasks.stream().collect(Collectors.groupingBy(Task::getLpuId));
        log.info("Запуск проверки. Найдено задач: {}, распределено по ЛПУ: {}", activeTasks.size(), tasksByLpu.size());

        List<CompletableFuture<Void>> futures = tasksByLpu.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> processSingleLpuTasks(entry.getKey(), entry.getValue())))
                .toList();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Превышен лимит времени ожидания завершения параллельного опроса ЛПУ", e);
        }
    }

    /**
     * Логика обработки задач внутри ОДНОГО конкретного ЛПУ (выполняется изолированно в своем потоке)
     */
    private void processSingleLpuTasks(String lpuId, List<Task> tasks) {
        log.debug("Поток для ЛПУ {} запущен. Задач к обработке: {}", lpuId, tasks.size());

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);

            // Если это не первая задача в данном ЛПУ — делаем паузу (Jitter) перед запросом.
            // Разные ЛПУ идут параллельно, но внутри одного ЛПУ запросы плавно идут друг за другом.
            if (i > 0) {
                try {
                    long delay = ThreadLocalRandom.current().nextLong(1000, 3501); // 1.0 - 3.5 секунды
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Опрос для ЛПУ {} был прерван", lpuId);
                    return;
                }
            }

            try {
                checkSingleTask(task, lpuId);
            } catch (Exception e) {
                log.error("Ошибка при проверке задачи ID {} для ЛПУ {}", task.getId(), lpuId, e);
            }
        }
        log.debug("Поток для ЛПУ {} успешно завершил работу.", lpuId);
    }

    /**
     * Логика проверки одной задачи
     */
    private void checkSingleTask(Task task, String lpuId) {
        log.info("Checking appointment for task {}", task.getId());
        List<Appointment> allAppointments = getAppointments(task, lpuId);

        if (allAppointments == null || allAppointments.isEmpty()) {
            log.info("No appointments found for task {}", task.getId());
            return;
        }

        List<Appointment> hardFilteredList = hardFiltering(allAppointments, task);
        if (hardFilteredList.isEmpty()) {
            log.info("Appointment not found for task {} on hard filter. Repeat search again later", task.getId());
            return;
        }

        String patientId = task.getMedicalCard().getPatientId();
        java.util.concurrent.atomic.AtomicReference<Appointment> appointment = new java.util.concurrent.atomic.AtomicReference<>();

        softFiltering(hardFilteredList, task).ifPresentOrElse(
                appointment1 -> createAppointmentWithPreferenceTime(task, appointment1, appointment, lpuId, patientId),
                () -> createAppointmentWithoutPreferenceTime(task, hardFilteredList, appointment, lpuId, patientId)
        );
    }

    private List<Appointment> getAppointments(Task task, String lpuId) {
        try {
            return gorzdravService.getAppointments(lpuId, task.getDoctorId());
        } catch (org.springframework.web.server.ResponseStatusException e) {
            log.warn("Горздрав вернул ошибку для ЛПУ {} и врача {}", lpuId, task.getDoctorId());
            return null;
        }
    }

    private void createAppointmentWithoutPreferenceTime(Task task, List<Appointment> hardFilteredList,
                                                        AtomicReference<Appointment> appointment,
                                                        String lpuId, String patientId) {
        try {
            Appointment appointment1 = hardFilteredList.get(0);
            appointment.set(appointment1);
            gorzdravService.createAppointment(appointment1, lpuId, patientId);
            doCompleteTaskAndNotifyUser(task, appointment, null);
            log.info("Appointment was created for any free time, no preferred time was found, task = {}",
                    task.getId());
        } catch (ResponseStatusException e) {
            doCompleteTaskAndNotifyUser(task, appointment, e.getMessage());
            log.error("Appointment was found, but gorzdrav response error in processing create appointment, " +
                    "task = {}, error = {}", task.getId(), e.getMessage());
        }
    }

    private void createAppointmentWithPreferenceTime(Task task, Appointment appointment1,
                                                     AtomicReference<Appointment> appointment,
                                                     String lpuId, String patientId) {
        try {
            appointment.set(appointment1);
            gorzdravService.createAppointment(appointment1, lpuId, patientId);
            doCompleteTaskAndNotifyUser(task, appointment, null);
            log.info("The appointment was created for appointment1 preferred time, task = {}",
                    task.getId());
        } catch (Exception e) {
            doCompleteTaskAndNotifyUser(task, appointment, e.getMessage());
            log.error("Appointment was found, but gorzdrav response error in processing create appointment, " +
                    "task = {}, error = {}", task.getId(), e.getMessage());
        }
    }

    private void doCompleteTaskAndNotifyUser(Task task, AtomicReference<Appointment> appointment, String errorText) {
        task.doFinished();
        taskRepository.save(task);

        if (errorText == null) {
            telegramAsyncMessageSender.sendMessageToUser(task.getOwner().getChatId(),
                    getMessageByAppointment(appointment.get(), task));
        } else {
            telegramAsyncMessageSender.sendMessageToUser(task.getOwner().getChatId(),
                    ERROR_MESSAGE_TEXT.formatted(task.getId(), errorText));
        }
    }

    private String getMessageByAppointment(Appointment appointment, Task task) {
        MedicalCard medicalCard = task.getMedicalCard();
        Long taskId = task.getId();
        var doctorInfo = doctorDictRepository.getDoctorInfoByDoctorExternalIdAndLpuExternalId(task.getDoctorId(),
                task.getLpuId());

        if (doctorInfo == null) {
            return MESSAGE_TEXT.formatted(appointment.address(),
                    FIRST_DATE_FORMAT.format(appointment.visitStart()) + " - " + SECOND_DATE_FORMAT.format(appointment.visitEnd()),
                    medicalCard.getLastName() + " " + medicalCard.getFirstName() + " " + medicalCard.getMiddleName(), taskId);
        }
        return FULL_MESSAGE_TEXT.formatted(appointment.address(),
                FIRST_DATE_FORMAT.format(appointment.visitStart()) + " - " + SECOND_DATE_FORMAT.format(appointment.visitEnd()),
                medicalCard.getLastName() + " " + medicalCard.getFirstName() + " " + medicalCard.getMiddleName(), taskId,
                doctorInfo.doctor().name(), doctorInfo.specialty().name());
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
