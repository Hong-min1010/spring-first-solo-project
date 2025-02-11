package com.springboot.answer.entity;

import com.springboot.question.entity.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(length = 300, nullable = false)
    private String answerContext;

    @OneToOne
    @JoinColumn(name = "QUESTION_ID")
    private Question question;
}
