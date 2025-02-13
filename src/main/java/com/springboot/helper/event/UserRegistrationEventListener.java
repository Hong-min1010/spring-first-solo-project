package com.springboot.helper.event;

import com.springboot.helper.email.EmailSender;
import com.springboot.user.entity.User;
import com.springboot.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@EnableAsync
@Configuration
@Component
@Slf4j
public class UserRegistrationEventListener {
    private final EmailSender emailSender;
    private final UserService userService;

    public UserRegistrationEventListener(EmailSender emailSender, UserService userService) {
        this.emailSender = emailSender;
        this.userService = userService;
    }

    @Async
    @EventListener
    public void listen(UserRegistrationApplicationEvent event) throws Exception {
        try {
            // 전송할 메시지를 생성했다고 가정.
            String message = "any email message";
            emailSender.sendEmail(message);
        } catch (MailSendException e) {
            e.printStackTrace();
            log.error("MailSendException: rollback for Member Registration:");
            User user = event.getUser();
            userService.deleteUser(user.getUserId());
        }
    }
}
