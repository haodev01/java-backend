package com.elearning.logging;

import com.elearning.model.RequestLog;
import com.elearning.service.AuditLogService;
import com.elearning.service.RequestLogService;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("ACCESS_LOG");
    // Không có khái niệm "vô hạn" thật sự ở tầng Servlet — đây là giới hạn AN TOÀN
    // (10MB) để chặn tình huống cực đoan (request cố tình gửi khổng lồ làm tràn bộ
    // nhớ), KHÔNG phải giới hạn để cắt bớt log — mọi payload thực tế đều log đủ.
    private static final int MAX_CACHE_BYTES = 10_000_000;
    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final AuditLogService auditLogService;
    private final RequestLogService requestLogService;

    public RequestLoggingFilter(AuditLogService auditLogService, RequestLogService requestLogService) {
        this.auditLogService = auditLogService;
        this.requestLogService = requestLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Spring Framework 7: constructor giờ bắt buộc truyền giới hạn cache (byte).
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_CACHE_BYTES);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId); // mọi log statement khác trong request này tự động mang theo requestId
        response.setHeader("X-Request-Id", requestId); // để bạn/support tra lại đúng request khi debug

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long latencyMs = System.currentTimeMillis() - start;
            Map<String, Object> fields = buildFields(wrappedRequest, wrappedResponse, requestId, latencyMs);
            logToConsole(fields);
            persistToDb(fields);
            auditIfAdmin(wrappedRequest, requestId);

            // BẮT BUỘC: nếu thiếu dòng này, client nhận response RỖNG dù Controller
            // đã trả dữ liệu bình thường — ContentCachingResponseWrapper chặn output
            // gốc lại để cache, phải tự tay copy ngược ra ngoài.
            wrappedResponse.copyBodyToResponse();
            MDC.clear(); // tránh requestId rò sang request tiếp theo dùng chung thread
        }
    }

    private Map<String, Object> buildFields(ContentCachingRequestWrapper req, ContentCachingResponseWrapper res,
                                            String requestId, long latencyMs) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("requestId", requestId);
        fields.put("method", req.getMethod());
        fields.put("path", req.getRequestURI());
        fields.put("status", res.getStatus());
        fields.put("latencyMs", latencyMs);
        fields.put("ip", req.getRemoteAddr());
        fields.put("userEmail", currentUserEmail());
        fields.put("userAgent", req.getHeader("User-Agent"));
        fields.put("requestHeaders", SensitiveDataMasker.maskHeaders(headersOf(req)));
        fields.put("requestBody", isMultipart(req) ? "[multipart — không log]"
                : SensitiveDataMasker.maskJson(bodyOf(req.getContentAsByteArray())));
        fields.put("responseBody", SensitiveDataMasker.maskJson(bodyOf(res.getContentAsByteArray())));
        return fields;
    }

    private void logToConsole(Map<String, Object> fields) {
        // "access_log" chỉ là message — toàn bộ dữ liệu thật nằm trong structured
        // arguments, LogstashEncoder tự đưa mỗi key trong `fields` thành 1 field JSON
        // TOP-LEVEL (vd "status":200), khác với nhét cả object vào 1 field "message"
        // dạng chuỗi — quan trọng để sau này lọc/query log được (vd status=500).
//        log.info("access_log", StructuredArguments.entries(fields));
    }

    private void persistToDb(Map<String, Object> fields) {
        try {
            RequestLog entry = new RequestLog(
                    (String) fields.get("requestId"),
                    (String) fields.get("method"),
                    (String) fields.get("path"),
                    (Integer) fields.get("status"),
                    (Long) fields.get("latencyMs"),
                    (String) fields.get("ip"),
                    (String) fields.get("userEmail"),
                    (String) fields.get("userAgent"),
                    toJsonString(fields.get("requestHeaders")),
                    toJsonString(fields.get("requestBody")),
                    toJsonString(fields.get("responseBody"))
            );
            requestLogService.record(entry);
        } catch (Exception e) {
            // QUAN TRỌNG: logging không bao giờ được phép làm hỏng response thật.
            // Nếu ghi DB lỗi (vd MySQL tạm gián đoạn), chỉ cảnh báo — request của
            // user vẫn phải trả về bình thường, console log vẫn còn nguyên vẹn.
            log.warn("Không lưu được request log vào DB: {}", e.getMessage());
        }
    }

    private String toJsonString(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private void auditIfAdmin(ContentCachingRequestWrapper req, String requestId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            auditLogService.record(requestId, auth.getName(), "ADMIN_ACTION", null,
                    req.getRemoteAddr(), req.getMethod() + " " + req.getRequestURI());
        }
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Spring Security LUÔN có một Authentication object khi request đi qua hết
        // chain, kể cả chưa đăng nhập — đó là AnonymousAuthenticationToken, không
        // phải null. So sánh đúng loại này mới nhận diện đúng "chưa đăng nhập".
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return auth.getName();
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith("multipart/");
    }

    private Map<String, String> headersOf(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        var names = request.getHeaderNames();
        if (names == null) return headers;
        Collections.list(names).forEach(name -> headers.put(name, request.getHeader(name)));
        return headers;
    }

    private String bodyOf(byte[] content) {
        if (content == null || content.length == 0) return "";
        // Không cắt bớt — log toàn bộ nội dung đã được cache (tối đa MAX_CACHE_BYTES
        // ở tầng wrapper phía trên, xem doFilterInternal).
        return new String(content, StandardCharsets.UTF_8);
    }
}