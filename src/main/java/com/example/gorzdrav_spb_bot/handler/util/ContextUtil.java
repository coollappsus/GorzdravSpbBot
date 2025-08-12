package com.example.gorzdrav_spb_bot.handler.util;

import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import org.springframework.stereotype.Service;

@Service
public class ContextUtil {

    public void cleanAllContext(UserState userState) {
        userState.getContext().clear();
    }

    public void cleanContext(UserState userState, Object... args) {
        for (Object arg : args) {
            userState.getContext().remove(arg);
        }
    }
}
