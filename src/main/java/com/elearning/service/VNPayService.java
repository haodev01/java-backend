package com.elearning.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class VNPayService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    public String createPaymentUrl(String txnRef, long amountVnd, String orderInfo, String ipAddress) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        // VNPay yêu cầu nhân 100 (quy ước chung cho mọi loại tiền tệ của họ,
        // không riêng gì VND) — QUÊN nhân 100 là lỗi rất hay gặp, số tiền
        // hiển thị trên trang VNPay sẽ sai lệch 100 lần so với ý định.
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));

        String query = buildQuery(params);
        String secureHash = hmacSHA512(hashSecret, query);
        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    // Verify chữ ký khi VNPay gọi IPN về — tính LẠI hash từ params nhận được
    // (trừ chính vnp_SecureHash) rồi so sánh với hash VNPay gửi kèm. Khớp
    // nghĩa là dữ liệu THẬT SỰ đến từ VNPay và KHÔNG bị ai can thiệp giữa
    // đường — đây là lớp bảo vệ quan trọng nhất của toàn bộ luồng thanh toán.
    public boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        TreeMap<String, String> toVerify = new TreeMap<>(params);
        toVerify.remove("vnp_SecureHash");
        toVerify.remove("vnp_SecureHashType");

        String query = buildQuery(toVerify);
        String computedHash = hmacSHA512(hashSecret, query);
        return computedHash.equalsIgnoreCase(receivedHash);
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash); // Java 17+ — không cần tự viết vòng lặp hex thủ công
        } catch (Exception e) {
            throw new IllegalStateException("Không tính được HMAC-SHA512", e);
        }
    }
}
