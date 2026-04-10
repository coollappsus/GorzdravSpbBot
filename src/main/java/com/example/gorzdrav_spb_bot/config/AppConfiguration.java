package com.example.gorzdrav_spb_bot.config;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация приложения
 *
 */
@Configuration
public class AppConfiguration {

    @Bean
    public VkApiClient vkApiClient() {
        return new VkApiClient(new HttpTransportClient());
    }

    @Bean
    public GroupActor groupActor(@Value("${vk.group-id}") Long groupId,
                                 @Value("${vk.access-token}") String accessToken) {
        return new GroupActor(groupId, accessToken);
    }
}