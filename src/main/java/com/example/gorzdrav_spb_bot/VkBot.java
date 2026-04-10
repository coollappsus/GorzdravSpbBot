package com.example.gorzdrav_spb_bot;

import com.example.gorzdrav_spb_bot.handler.MessageHandler;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class VkBot {

    private final VkApiClient vk;
    private final GroupActor actor;
    private final MessageHandler messageHandler;

    @EventListener(ApplicationReadyEvent.class)
    @Async // Запускаем в отдельном потоке, чтобы не блокировать старт приложения
    public void run() {
        log.info("Запуск VK Long Poll...");
        try {
            Integer ts = Integer.valueOf(vk.groups().getLongPollServer(actor, actor.getGroupId()).execute().getTs());
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    var historyResponse = vk.messages().getLongPollHistory(actor).ts(ts).execute();
                    var messages = historyResponse.getMessages().getItems();

                    if (!messages.isEmpty()) {
                        messages.forEach(messageHandler::handle);
                    }
                    ts = historyResponse.getNewPts(); // Обновляем временную метку
                } catch (Exception e) {
                    log.error("Ошибка при получении сообщений, пробуем снова...", e);
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            log.error("Критическая ошибка Long Poll", e);
        }
    }
}
