package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.cache.util.ContextUtil;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.repository.UserRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class AddMedCardLpuHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH = """
                Поздравляю! Медицинская карта успешно добавлена✨
                Теперь используй ее для записи к врачу🚑
                """;

    private final GorzdravService gorzdravService;
    private final MedicalCardRepository medicalCardRepository;
    private final UserRepository userRepository;
    private final StartHandler startHandler;
    private final ContextUtil contextUtil;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        District district = userState.getDistrict();
        String lpuName = message.getText().substring(0, message.getText().indexOf(" по адресу"));
        LPU lpu = gorzdravService.getLPUs(district).stream()
                .filter(l -> l.lpuShortName().equals(lpuName))
                .findFirst()
                .orElseThrow();

        MedicalCardContext medicalCardContext = userState.getMedicalCardContext();
        var patientId = gorzdravService.findPatient(lpu, medicalCardContext);
        if (patientId == null) {
            throw new RuntimeException("Медицинская карта по введенным данным не найдена");
        }
        var user = userRepository.findUserByUserId(medicalCardContext.getOwner().userId());
        medicalCardRepository.save(
                MedicalCard.builder()
                        .patientId(patientId)
                        .lpuId(Integer.parseInt(lpu.id()))
                        .owner(user)
                        .birthDate(medicalCardContext.getBirthDate())
                        .middleName(medicalCardContext.getMiddleName())
                        .firstName(medicalCardContext.getFirstName())
                        .lastName(medicalCardContext.getLastName())
                        .build());

        userState.setHandlerName("StartHandler");
        contextUtil.cleanAllContext(userState);
        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH);
        return startHandler.processMessage(message, userState);
    }
}
