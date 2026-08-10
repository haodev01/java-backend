package com.elearning.service;

import com.elearning.dto.PageResponse;
import com.elearning.exception.NotFoundException;
import com.elearning.model.RequestLog;
import com.elearning.repository.RequestLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class RequestLogService {
    private final RequestLogRepository requestLogRepository;
    public RequestLogService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    public  void record(RequestLog entry) {
     requestLogRepository.save(entry);
    }

    // Không @Cacheable — log ghi liên tục nên cache gần như luôn stale ngay
    // sau khi set, không giống courseList (ghi hiếm, đọc nhiều).
    public PageResponse<RequestLog> listLogs(Instant from, Instant to, boolean onlyErrors,
                                             Integer status, String userEmail, String method,
                                             String path, Pageable pageable) {
        return PageResponse.from(requestLogRepository.findAllFiltered(
                from, to, onlyErrors, status, userEmail, method, path, pageable));
    }

    public RequestLog getDetail(Long id) {
        return requestLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy log id=" + id));
    }
}
