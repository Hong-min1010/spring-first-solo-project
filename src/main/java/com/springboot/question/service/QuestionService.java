package com.springboot.question.service;

import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.like.repository.LikeRepository;
import com.springboot.question.entity.Question;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.user.repository.UserRepository;
import com.springboot.utils.CheckUserRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Transactional
@Service
@Slf4j
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CheckUserRoles checkUserRoles;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public QuestionService(QuestionRepository questionRepository,
                           CheckUserRoles checkUserRoles, LikeRepository likeRepository, UserRepository userRepository, StorageService storageService) {

        this.questionRepository = questionRepository;
        this.checkUserRoles = checkUserRoles;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    public Question createQuestion(Question question,
                                   CustomUserDetails customUserDetails,
                                   List<MultipartFile> files) {

        Long currentUserId = customUserDetails.getUserId();

        // 관리자 검증 (여기서 수정된 부분)
        if (checkUserRoles.isAdmin()) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        // 요청한 사용자가 맞는지 확인
        checkUserRoles.matchUserId(question.getUser().getUserId(), customUserDetails);

        List<String> imageUrls = new ArrayList<>();  // 이미지 URL을 저장할 리스트

        // 파일이 여러 개일 경우 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    log.info("Uploading file to S3...");
                    storageService.store(file);  // S3에 파일 업로드
                    // 내 Bucket 주소로 수정 要
                    String fileUrl = "s3://your-bucket-name/" + file.getOriginalFilename();
                    imageUrls.add(fileUrl);  // 파일 URL을 리스트에 추가
                }
            }
            question.setImageUrls(imageUrls);  // 이미지 URL들을 질문에 설정
        }

        return questionRepository.save(question);  // 질문 저장
    }

    public Question findQuestion(Long questionId, Long currentId) {

        Question question = findVerifiedQuestion(questionId);

        if (question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
            if (!question.getUser().getUserId().equals(currentId) && !checkUserRoles.isAdmin()) {
                throw new BusinessLogicException(ExceptionCode.FORBIDDEN_ACCESS);
            }
        }

        verifyQuestionDeleteStatus(question);

        if (question.getQuestionVisibility() != Question.QuestionVisibility.QUESTION_SECRET) {
            question.setViewCount(question.getViewCount() + 1);
        }


        questionRepository.save(question);


        return question;
    }

    public Page<Question> findQuestions(int page, int size) {
        // Secret Qeustion 은 조회되면 안됨

        // 전체 조회 時 SECRET Question "비공개글 입니다" 로 변환
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

        // login 한 사용자
        Long currentUserId = customUserDetails.getUserId();

        // 질문을 등록 한 사용자만 삭제 가능
        Question question = findVerifiedQuestion(questionId);

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

//    private QuestionResponseDto convertToResponseDto(Question question) {
//        List<Like> likes = likeRepository.findByQuestion(question);
//
//        AnswerResponseDto  answerResponseDto = null;
//        if (question.getAnswer() != null) {
//            Long answerId = question.getAnswer().getAnswerId();
//            String content = question.getAnswer().getAnswerContext(); // 예시로 content도 가져오는 것
//            Long userId = question.getAnswer().getUser().getUserId(); // 답변을 쓴 user의 ID
//            Long questionId = question.getQuestionId(); // 해당 질문의 ID
//            String userName = question.getAnswer().getUser().getName();  // answerId를 생성자에 전달
//        }
//
//        List<LikeResponseDto> likeDtos = likes.stream()
//                .map(like -> new LikeResponseDto(
//                        like.getLikeId(),
//                        like.getQuestion().getQuestionId(),
//                        like.getUser().getUserId()
//                )).collect(Collectors.toList());
//        return new QuestionResponseDto(question.getQuestionId(),
//                question.getTitle(),
//                question.getQuestionContext(),
//                question.getViewCount(),
//                question.getLikeCount(),
//                question.getQuestionStatus(),
//                question.getQuestionVisibility(),
//                question.getUser() != null ? question.getUser().getName() : null,
//                answerResponseDto,
//                likeDtos);
//    }

}
