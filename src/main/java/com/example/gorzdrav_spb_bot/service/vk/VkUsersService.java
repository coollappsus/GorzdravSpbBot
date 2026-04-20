package com.example.gorzdrav_spb_bot.service.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Slf4j
@AllArgsConstructor
public class VkUsersService {

    private final VkApiClient vkApiClient;
    private final GroupActor groupActor;

    public String getUserName(long userId) throws ClientException, ApiException {
        var query = vkApiClient.users().get(getUserActor(userId)).userIds(String.valueOf(userId)).execute();
        if (query != null && !query.isEmpty()) {
            return query.get(0).getFirstName() + " " + query.get(0).getLastName();
        }
        throw new NoSuchElementException("User not found");
    }

    private UserActor getUserActor(long userId) {
        return new UserActor(userId, groupActor.getAccessToken());
    }
}
