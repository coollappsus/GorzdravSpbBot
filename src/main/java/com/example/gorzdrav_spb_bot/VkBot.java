package com.example.gorzdrav_spb_bot;

import com.example.gorzdrav_spb_bot.handler.MessageHandler;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

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
            Integer ts = vk.messages().getLongPollServer(actor).execute().getTs();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    MessagesGetLongPollHistoryQuery historyQuery = vk.messages().getLongPollHistory(actor).ts(ts);
                    List<Message> messages = historyQuery.execute().getMessages().getItems();

                    if (!messages.isEmpty()) {
                        messages.forEach(messageHandler::handle);
                    }
                    ts = vk.messages().getLongPollServer(actor).execute().getTs();
                } catch (Exception e) {
                    log.error("Ошибка при получении сообщений, пробуем снова...", e);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            log.error("Критическая ошибка Long Poll", e);
        }
    }
}
