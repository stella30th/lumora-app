package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.progress.ReviewStatsResponse;
import com.angeltlh31.lumora.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @Operation(summary = "Thong ke on tap cua nguoi dung dang dang nhap: streak, so lan on hom nay, lich su 30 ngay")
    @GetMapping("/stats")
    public ResponseEntity<ReviewStatsResponse> getStats(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(progressService.getStats(userId));
    }
}
