package com.elearning.controller;

import com.elearning.dto.ApiResponse;
import com.elearning.dto.PageResponse;
import com.elearning.model.RequestLog;
import com.elearning.service.RequestLogService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/request-logs")
@PreAuthorize("hasRole('ADMIN')")
public class RequestLogController {

    private final RequestLogService requestLogService;

    public RequestLogController(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestLog>>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false, defaultValue = "false") boolean onlyErrors,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String path,
            // sort mặc định occurredAt DESC — khớp đúng idx_request_logs_occurred_at,
            // tránh filesort khi bảng ngày càng nhiều dòng.
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("OK",
                requestLogService.listLogs(from, to, onlyErrors, status, userEmail, method, path, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestLog>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", requestLogService.getDetail(id)));
    }
}
