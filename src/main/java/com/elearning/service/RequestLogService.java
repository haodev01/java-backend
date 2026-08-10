package com.elearning.service;

import com.elearning.model.RequestLog;
import com.elearning.repository.RequestLogRepository;
import org.springframework.stereotype.Service;


@Service
public class RequestLogService {
    private final RequestLogRepository requestLogRepository;
    public RequestLogService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    public  void record(RequestLog entry) {
     requestLogRepository.save(entry);
    }
}
