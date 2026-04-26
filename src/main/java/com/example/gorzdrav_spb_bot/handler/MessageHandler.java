package com.example.gorzdrav_spb_bot.handler;

import api.longpoll.bots.model.objects.basic.Message;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static com.example.gorzdrav_spb_bot.config.Const.CURRENT_GROUP_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageHandler {

    private static Set<UserState> states = new HashSet<>();

    private final VkApiClient vk;
    private final GroupActor actor;
    private final UpdateMessageDispatcher updateMessageDispatcher;
    private final StartHandler startHandler;
    private final Random random = new Random();

    public void handle(Message message) {
        String text = message.getText();
        Long peerId = Long.valueOf(message.getPeerId());

        if (Objects.equals(peerId, CURRENT_GROUP_ID)) {
            //TODO: Иногда, не всегда, ловим интересную штуку. Сервис нашел номерок и асинхронно записал к врачу,
            // оповестил об этом пользователя(!) от имени бота, а вк подхватывает это сообщение, как новое для бота
            // и пытается его бесконечно обработать. Надо наблюдать за этим поведением. Пока заколотим костылем.
            log.info("The message was received from the group itself " + peerId);
            return;
        }

        log.info("Новое сообщение от {}: {}", peerId, text);
        sendMessage(peerId, processUpdate(message));
    }

    private void sendMessage(Long peerId, VkResponse response) {
        try {
            vk.messages().sendDeprecated(actor)
                    .peerId(peerId)
                    .message(response.getMessage())
                    .randomId(random.nextInt())
                    .keyboard(response.getKeyboard())
                    .execute();
        } catch (Exception e) {
            log.error("Error while processing update", e);
            try {
                sendUserErrorMessage(peerId, e.getMessage());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private VkResponse processUpdate(Message message) {
        Long userId = Long.valueOf(message.getFromId());
        UserState state = states.stream().filter(s -> s.getUserId().equals(userId)).findFirst()
                .orElse(null);
        if (state == null) {
            state = UserState.builder()
                    .handler(startHandler)
                    .context(new HashSet<>())
                    .userId(userId)
                    .build();
            states.add(state);
        }

        return updateMessageDispatcher.processMessage(message, state);
    }

    private void sendUserErrorMessage(Long userId, String errorMessage) throws ClientException, ApiException {
        vk.messages().sendDeprecated(actor)
                .userId(userId)
                .message("Произошла ошибка, попробуйте позже или обратитесь к админу. \nОшибка: " + errorMessage)
                .randomId(new Random().nextInt())
                .execute();
    }
}
