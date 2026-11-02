package com.example.gorzdrav_spb_bot.handler.handlers.tracking.find;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.UserStateService;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.TaskContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.repository.DoctorDictRepositoryImpl;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.TRACKING_CANCEL;

@Component
@AllArgsConstructor
public class TrackingFindHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_HEADER = "📝Текущие отслеживания:\n";
    private static final String RESPONSE_TEXT_NOT_TRACKING = "📝Отслеживаний не найдено";
    private static final String DATA_FETCH_ERROR_MESSAGE = "Не удалось получить данные";

    private final KeyboardFactory keyboardFactory;
    private final TaskRepository taskRepository;
    private final DoctorDictRepositoryImpl doctorDictRepository;
    private final StartHandler startHandler;
    private final UserStateService userStateService;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        List<TaskContext> activeTasks = getActiveTasks(userState);
        if (activeTasks.isEmpty()) {
            userStateService.deleteByUserId(userState.getUserId());
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_NOT_TRACKING);
            return startHandler.processMessage(message, userState);
        }
        userState.setTasks(activeTasks);
        var keyboard = keyboardFactory.createReplyKeyboard(List.of(TRACKING_CANCEL.getText()));
        userState.setHandlerName("TrackingCancelChooseHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(getListStringTask(activeTasks))
                .build();
    }

    private List<TaskContext> getActiveTasks(UserState userState) {
        return taskRepository.findByOwnerIdAndActiveStatus(userState.getUserContext().id(), true)
                .stream()
                .map(t -> TaskContext.builder()
                        .id(t.getId())
                        .owner(UserContext.builder()
                                .userId(t.getOwner().getUserId())
                                .id(t.getId())
                                .chatId(t.getOwner().getChatId())
                                .userName(t.getOwner().getUserName())
                                .build())
                        .lpuId(t.getLpuId())
                        .activeStatus(t.isActiveStatus())
                        .timePreference(t.getTimePreference())
                        .completeStatus(t.isCompleteStatus())
                        .doctorId(t.getDoctorId())
                        .preferenceDate(t.getPreferenceDate())
                        .medicalCard(MedicalCardContext.builder()
                                .birthDate(t.getMedicalCard().getBirthDate())
                                .middleName(t.getMedicalCard().getMiddleName())
                                .firstName(t.getMedicalCard().getFirstName())
                                .lastName(t.getMedicalCard().getLastName())
                                .patientId(t.getMedicalCard().getPatientId())
                                .id(t.getMedicalCard().getId())
                                .lpuId(t.getMedicalCard().getLpuId())
                                .build())
                        .build())
                .toList();
    }

    private String getListStringTask(List<TaskContext> taskList) {
        StringBuilder sb = new StringBuilder();
        sb.append(RESPONSE_TEXT_HEADER);

        for (TaskContext task : taskList) {
            String preferenceDateString = Optional.ofNullable(task.getPreferenceDate())
                    .map(date -> new SimpleDateFormat("d MMMM yyyy", new Locale("ru")).format(date))
                    .orElse("Любая");
            var doctorInfo = doctorDictRepository.getDoctorInfoByDoctorExternalIdAndLpuExternalId(task.getDoctorId(),
                    task.getLpuId(), true);

            sb.append("Номер отслеживания: №").append(task.getId()).append(".\n")
                    .append("👤ФИО - ").append(task.getMedicalCard().getFIO()).append("\n")
                    .append("⏱️Предпочитаемое время - ").append(task.getTimePreference().getAdditionalName()).append("\n")
                    .append("📅Дата отслеживания - ").append(preferenceDateString).append("\n")
                    .append("👩‍⚕️Доктор - ").append(
                            Optional.ofNullable(doctorInfo).map(i -> i.doctor().name()).orElse(DATA_FETCH_ERROR_MESSAGE))
                    .append("\n")
                    .append("🩺Специальность - ").append(
                            Optional.ofNullable(doctorInfo).map(i -> i.specialty().name()).orElse(DATA_FETCH_ERROR_MESSAGE))
                    .append("\n")
                    .append("🏥Лечебно-профилактическое учреждение - ").append(
                            Optional.ofNullable(doctorInfo).map(i -> i.lpu().lpuShortName()).orElse(DATA_FETCH_ERROR_MESSAGE))
                    .append("\n")
                    .append(" по адресу - ").append(Optional.ofNullable(doctorInfo).map(i -> i.lpu().address()).orElse(DATA_FETCH_ERROR_MESSAGE))
                    .append("\n\n");
        }
        return sb.toString();
    }
}
