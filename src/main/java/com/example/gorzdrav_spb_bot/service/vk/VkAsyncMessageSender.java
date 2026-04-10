package com.example.gorzdrav_spb_bot.service.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class VkAsyncMessageSender {

    private final VkApiClient vk;
    private final GroupActor actor;
    private final Random rand = new Random();

    /**
     * Удобный метод для отправки сообщений пользователю асинхронно
     */
    @Async
    public void sendMessageToUser(Long peerId, String text) {
        try {
            var sendQuery = vk.messages().sendDeprecated(actor)
                    .message(text)
                    .peerId(peerId)
                    .randomId(rand.nextInt(10000));

            sendQuery.execute();
        } catch (Exception e) {
            log.error("Ошибка отправки: ", e);
        }
    }
}
