package com.springboot.question.dto;

import com.springboot.answer.entity.Answer;
import com.springboot.like.entity.Like;
import com.springboot.user.entity.User;
import lombok.Getter;

import javax.validation.constraints.Positive;
import java.util.List;


@Getter
public class QuestionPostDto {

    @Positive
    // 누가 question을 작성했는지 식별하기 위한 Id
    private Long userId;

    private String questionContext;

    private int viewCount;

    private List<Like> likes;

    private Answer answer;

//    public User getUser() {
//        User user = new User();
//        user.setUserId(userId);
//
//        return user;
//    }
}
