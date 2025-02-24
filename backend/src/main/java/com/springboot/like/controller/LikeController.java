package com.springboot.like.controller;

import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.like.dto.LikeResponseDto;
import com.springboot.like.entity.Like;
import com.springboot.like.mapper.LikeMapper;
import com.springboot.like.service.LikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/questions")
public class LikeController {
    private final LikeService likeService;
    private final LikeMapper likeMapper;

    public LikeController(LikeService likeService, LikeMapper likeMapper) {
        this.likeService = likeService;
        this.likeMapper = likeMapper;
    }

    @PostMapping("/{question-id}/likes")
    public ResponseEntity<LikeResponseDto> addLike(@PathVariable("question-id") Long questionId,
                                                   @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getUserId();

        System.out.println("QuestionId: " + questionId);
        System.out.println("UserId: " + userId);

        Like like = likeService.addLike(userId, questionId);

        System.out.println("Like Object: " + like);

        LikeResponseDto responseDto = likeMapper.likeToLikeResponseDto(like);

        System.out.println("Response DTO: " + responseDto);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{question-id}/likes")
    public ResponseEntity<Void> removeLike(@PathVariable("question-id") Long questionId,
                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getUserId();

        likeService.removeLike(userId, questionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
}
