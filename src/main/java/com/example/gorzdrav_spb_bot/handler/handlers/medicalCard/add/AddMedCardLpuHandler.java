package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class AddMedCardLpuHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH = """
                Поздравляю! Медицинская карта успешно добавлена✨
                Теперь используй ее для записи к врачу🚑
                """;

    private final GorzdravService gorzdravService;
    private final MedicalCardRepository medicalCardRepository;
    private final StartHandler startHandler;
    private final ContextUtil contextUtil;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    public AddMedCardLpuHandler(GorzdravService gorzdravService, MedicalCardRepository medicalCardRepository,
                                @Lazy StartHandler startHandler, ContextUtil contextUtil,
                                TelegramAsyncMessageSender telegramAsyncMessageSender) {
        this.gorzdravService = gorzdravService;
        this.medicalCardRepository = medicalCardRepository;
        this.startHandler = startHandler;
        this.contextUtil = contextUtil;
        this.telegramAsyncMessageSender = telegramAsyncMessageSender;
    }

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        District district = userState.getContext().stream()
                .filter(d -> d instanceof District)
                .map(d -> (District) d)
                .findFirst()
                .orElseThrow();
        String lpuName = message.getText().substring(0, message.getText().indexOf(" по адресу"));
        LPU lpu = gorzdravService.getLPUs(district).stream()
                .filter(l -> l.lpuShortName().equals(lpuName))
                .findFirst()
                .orElseThrow();

        MedicalCard medicalCard = userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map((mc) -> (MedicalCard) mc)
                .findFirst()
                .orElseThrow();
        var patientId = gorzdravService.findPatient(lpu, medicalCard);
        if (patientId == null) {
            throw new RuntimeException("Медицинская карта по введенным данным не найдена");
        }
        medicalCard.setPatientId(patientId);
        medicalCard.setLpuId(Integer.parseInt(lpu.id()));
        medicalCardRepository.save(medicalCard);

        userState.setHandler(startHandler);
        contextUtil.cleanAllContext(userState);
        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH);
        return startHandler.processMessage(message, userState);
    }
}
