package com.springboot.helper.event;

import com.springboot.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegistrationApplicationEvent extends ApplicationEvent {
    private User user;
    public UserRegistrationApplicationEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
