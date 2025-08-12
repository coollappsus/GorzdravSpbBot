package com.example.gorzdrav_spb_bot.handler;

import lombok.Getter;

@Getter
public enum UserConstResponseText {
    ADD("Добавить"),
    CREATE_APPOINTMENT("Записаться к врачу"),
    FIND_APPOINTMENT("Посмотреть текущие записи"),
    CANCEL_APPOINTMENT("Отменить запись к врачу"),
    TRACKING_APPOINTMENT("Отслеживание и запись к врачу при появлении талончиков"),
    REMOVE("Удалить карту"),
    TO_MAIN("В начало"),
    CONFIRMATION("Подтвердить");


    public final String text;

    UserConstResponseText(String text) {
        this.text = text;
    }
}
