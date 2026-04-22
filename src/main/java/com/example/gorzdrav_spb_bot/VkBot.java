package com.example.gorzdrav_spb_bot;

import com.example.gorzdrav_spb_bot.handler.MessageHandler;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
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

import static com.example.gorzdrav_spb_bot.config.Const.ADMIN_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VkBot {

    private final VkApiClient vk;
    private final GroupActor actor;
    private final MessageHandler messageHandler;
    private final VkAsyncMessageSender vkAsyncMessageSender;

    @EventListener(ApplicationReadyEvent.class)
    @Async // Запускаем в отдельном потоке, чтобы не блокировать старт приложения
    public void run() {
        log.info("Запуск VK Long Poll...");
        try {
            Integer ts = vk.messages().getLongPollServer(actor).execute().getTs();
            int errorCounter = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    MessagesGetLongPollHistoryQuery historyQuery = vk.messages().getLongPollHistory(actor).ts(ts);
                    List<Message> messages = historyQuery.execute().getMessages().getItems();

                    if (!messages.isEmpty()) {
                        messages.forEach(messageHandler::handle);
                    }
                    ts = vk.messages().getLongPollServer(actor).execute().getTs();
                    errorCounter = 0;
                } catch (Exception e) {
                    sendMessageError(e);
                    log.error("Ошибка при получении сообщений, штатная ситуация, пробуем снова...", e);
                    Thread.sleep(10000);

                    if (errorCounter++ >= 10) {
                        Thread.currentThread().interrupt();
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            log.error("Критическая ошибка Long Poll", e);
        }
    }

    public void sendMessageError(Exception e) {
        vkAsyncMessageSender.sendMessageToUser(ADMIN_ID, e.getMessage());
    }
}
