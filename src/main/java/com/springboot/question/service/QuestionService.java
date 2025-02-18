package com.springboot.question.service;

import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.repository.AnswerRepository;
import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.like.dto.LikeResponseDto;
import com.springboot.like.entity.Like;
import com.springboot.like.repository.LikeRepository;
import com.springboot.question.dto.QuestionResponseDto;
import com.springboot.question.entity.Question;
import com.springboot.question.mapper.QuestionMapper;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.user.entity.User;
import com.springboot.user.repository.UserRepository;
import com.springboot.user.service.UserService;
import com.springboot.utils.CheckUserRoles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CheckUserRoles checkUserRoles;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public QuestionService(QuestionRepository questionRepository,
                           CheckUserRoles checkUserRoles, LikeRepository likeRepository, UserRepository userRepository) {

        this.questionRepository = questionRepository;
        this.checkUserRoles = checkUserRoles;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    public Question createQuestion(Question question,
                                   CustomUserDetails customUserDetails) {

        Long currentUserId = customUserDetails.getUserId();

//        System.out.println("Current UserId: " +currentUserId);

        if (checkUserRoles.isAdmin()) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

//        System.out.println("Requested UserId: " + question.getUser().getUserId());
        checkUserRoles.matchUserId(question.getUser().getUserId(), customUserDetails);
//        System.out.println("Comparing UserIds -> Question Owner: " + question.getUser().getUserId() + ", Current User: " + currentUserId);
        return questionRepository.save(question);
    }

    public Question findQuestion(Long questionId, Long currentId) {

        Question question = findVerifiedQuestion(questionId);

        if (question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
            if (!question.getUser().getUserId().equals(currentId) && !checkUserRoles.isAdmin()) {
                question.setQuestionContext("비밀글입니다.");
            }
        }

//        Long userId = question.getUser().getUserId();

        verifyQuestionDeleteStatus(question);

        question.setViewCount(question.getViewCount() + 1);

        questionRepository.save(question);


        return question;
    }

    public Page<Question> findQuestions(int page, int size) {
        // Secret Qeustion 은 조회되면 안됨

        // 전체 조회 時 SECRET Question "비공개글 입니다" 로 변환 -> convertToResponseDto 사용 해야함

        Page<Question> questionPage = questionRepository.findByQuestionStatusNotIn(
                Arrays.asList(
                        Question.QuestionStatus.QUESTION_DELETED,
                        Question.QuestionStatus.QUESTION_DEACTIVED
                ),
                PageRequest.of(page - 1, size, Sort.by("questionId").descending()));

        List<Question> questions = questionPage.getContent();

        for (Question question : questions) {
            if (question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
                question.setTitle("SECRET");
                question.setQuestionContext("비공개글입니다.");
            }
        }

        return questionPage;
    }

    public void deleteQuestion(long questionId, CustomUserDetails customUserDetails) {
        // 작성자가 User인지 확인하는 메서드 (사용 안해도 됨)
//        checkUserRoles.isUser();
//        System.out.println("Received questionId: " + questionId);

        // login 한 사용자
        Long currentUserId = customUserDetails.getUserId();
//        System.out.println("현재 로그인한 사용자 ID: " + currentUserId);

        // 질문을 등록 한 사용자만 삭제 가능
        Question question = findVerifiedQuestion(questionId);
//        System.out.println("질문 작성자 ID: " + question.getUser().getUserId());

        if (!question.getUser().getUserId().equals(currentUserId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        question.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);

        questionRepository.save(question);
    }

    public Question updateQuestion(Long questionId, Long userId, Question question) {

        checkUserRoles.isUser();

        Question findQuestion = findVerifiedQuestion(questionId);

        if (findQuestion.getUser() == null || !findQuestion.getUser().getUserId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
        }

        if (findQuestion.getQuestionStatus() == Question.QuestionStatus.QUESTION_ANSWERED) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
        }

        Optional.ofNullable(question.getTitle())
                .ifPresent(findQuestion::setTitle);
        Optional.ofNullable(question.getQuestionContext())
                .ifPresent(findQuestion::setQuestionContext);

        findQuestion.setUser(findQuestion.getUser());

        return questionRepository.save(findQuestion);
    }

    public List<Question> getQuestionSorted(String sortBy) {
        switch (sortBy) {
            case "latest":
                return questionRepository.findAllByOrderByCreatedAtDesc();
            case "oldest":
                return questionRepository.findAllByOrderByCreatedAtAsc();
            case "likesDesc":
                return questionRepository.findAllByOrderByLikeCountDesc();
            case "likesAsc":
                return questionRepository.findAllByOrderByLikeCountAsc();
            case "viewsDesc":
                return questionRepository.findAllByOrderByViewCountDesc();
            case "viewsAsc":
                return questionRepository.findAllByOrderByViewCountAsc();
            default:
                throw new IllegalArgumentException("Invalid sort criteria");
        }
    }

    // LikeCount를 증가 시키는 메서드 생성
    public void addLikeCount(Question question) {
        question.setLikeCount(question.getLikeCount() + 1);
        questionRepository.save(question);
    }

    // LikeCount를 감소 시키는 메서드 생성
    public void decreaseCount(Question question) {
        question.setLikeCount(question.getLikeCount() - 1);
        questionRepository.save(question);
    }

    // 질문이 존재하는지 확인하는 메서드 생성
    public Question findVerifiedQuestion(Long questionId) {
        Optional<Question> optionalQuestion =
                questionRepository.findWithUserByQuestionId(questionId);  // questionId를 넘기도록 수정

        if (!optionalQuestion.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND);
        }
        return optionalQuestion.get();
    }

    public void verifyQuestionDeleteStatus(Question question) {
        if(question.getQuestionStatus() == Question.QuestionStatus.QUESTION_DELETED) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND);
        }
    }

    private QuestionResponseDto convertToResponseDto(Question question) {
        List<Like> likes = likeRepository.findByQuestion(question);

        AnswerResponseDto  answerResponseDto = null;
        if (question.getAnswer() != null) {
            Long answerId = question.getAnswer().getAnswerId();
            String content = question.getAnswer().getAnswerContext(); // 예시로 content도 가져오는 것
            Long userId = question.getAnswer().getUser().getUserId(); // 답변을 쓴 user의 ID
            Long questionId = question.getQuestionId(); // 해당 질문의 ID
            String userName = question.getAnswer().getUser().getName();  // answerId를 생성자에 전달
        }

        List<LikeResponseDto> likeDtos = likes.stream()
                .map(like -> new LikeResponseDto(
                        like.getLikeId(),
                        like.getQuestion().getQuestionId(),
                        like.getUser().getUserId()
                )).collect(Collectors.toList());
        return new QuestionResponseDto(question.getQuestionId(),
                question.getTitle(),
                question.getQuestionContext(),
                question.getViewCount(),
                question.getLikeCount(),
                question.getQuestionStatus(),
                question.getQuestionVisibility(),
                question.getUser() != null ? question.getUser().getName() : null,
                answerResponseDto,
                likeDtos);
    }

    public boolean isAuthorOrAdmin(Long questionId, String username) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        return user.getRoles().contains("ADMIN") || question.getUser().getUserId().equals(user.getUserId());
    }

}
