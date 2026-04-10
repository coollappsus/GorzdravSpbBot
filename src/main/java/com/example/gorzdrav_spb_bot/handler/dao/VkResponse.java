package com.example.gorzdrav_spb_bot.handler.dao;

import com.vk.api.sdk.objects.messages.Keyboard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VkResponse {

    private String message;
    private Keyboard keyboard;
}
