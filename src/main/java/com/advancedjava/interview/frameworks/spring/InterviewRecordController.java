package com.advancedjava.interview.frameworks.spring;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Web + record：
 * 1. 请求 DTO/响应 DTO 用 record，表达“不可变传输对象”。
 * 2. 校验注解可直接写在组件上。
 * 3. 控制器内部返回 record 响应，减少样板代码。
 */
@RestController
@RequestMapping("/record-demo/interviews")
public class InterviewRecordController {

    @PostMapping
    public CreateInterviewResponse create(@Valid @RequestBody CreateInterviewRequest request) {
        int normalizedScore = Math.max(0, Math.min(request.score(), 100));
        String level = normalizedScore >= 80 ? "PASS" : "PENDING";
        return new CreateInterviewResponse(
                UUID.randomUUID().toString(),
                request.candidateName().trim(),
                normalizedScore,
                level,
                Instant.now().toString()
        );
    }

    public record CreateInterviewRequest(
            @NotBlank String candidateName,
            @Min(0) @Max(100) int score,
            List<String> tags
    ) {
        public CreateInterviewRequest {
            tags = tags == null ? List.of() : List.copyOf(tags);
        }
    }

    public record CreateInterviewResponse(
            String interviewId,
            String candidateName,
            int score,
            String status,
            String createdAt
    ) {
    }
}
