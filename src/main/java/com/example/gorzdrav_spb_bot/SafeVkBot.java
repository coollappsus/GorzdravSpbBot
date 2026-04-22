package com.example.gorzdrav_spb_bot;


import com.example.gorzdrav_spb_bot.handler.MessageHandler;
import com.vk.api.sdk.callback.longpoll.CallbackApiLongPoll;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Наследуемся от стандартного обработчика. 
 * Вместо ручного цикла используем событийно-ориентированный подход.
 */
@Service
@Slf4j
public class SafeVkBot extends CallbackApiLongPoll {

    private final MessageHandler messageHandler;

    public SafeVkBot(VkApiClient vk, GroupActor actor, MessageHandler messageHandler)
            throws ClientException, ApiException {
        super(vk, actor);
        this.messageHandler = messageHandler;
        vk.groups().setLongPollSettings(actor).enabled(true)
                .wallPostNew(true)
                .messageNew(true)
                .execute();
    }

    /**
     * Переопределяем метод обработки сообщений.
     * Здесь нет цикла — метод вызывается только при поступлении ивента.
     */
    @Override
    public void messageNew(Integer groupId, Message message) {
        messageHandler.handle(message);
    }

    @Override
    public void run() {
        try {
            System.out.println("Запуск LongPoll...");
            super.run(); // Запускает библиотечный цикл
        } catch (Exception e) {
            // Если ВК упал, Spring завершит работу этого бина.
            // Без внешнего while(true) спама не будет.
            System.err.println("Критический сбой LongPoll: " + e.getMessage());
        }
    }
}
