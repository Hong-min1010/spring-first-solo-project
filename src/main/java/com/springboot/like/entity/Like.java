package com.springboot.like.entity;

import com.springboot.question.entity.Question;
import com.springboot.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @JoinColumn(name = "QUESTION_ID")
    @ManyToOne
    private Question question;

    @JoinColumn(name = "USER_ID")
    @ManyToOne
    private User user;
}
