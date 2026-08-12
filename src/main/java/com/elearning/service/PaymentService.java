package com.elearning.service;

import com.elearning.exception.NotFoundException;
import com.elearning.model.Order;
import com.elearning.model.OrderStatus;
import com.elearning.model.Payment;
import com.elearning.model.PaymentStatus;
import com.elearning.repository.OrderRepository;
import com.elearning.repository.PaymentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VNPayService vnPayService;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository,
                          VNPayService vnPayService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.vnPayService = vnPayService;
    }

    @Transactional
    public String createPaymentUrl(String requesterEmail, Long orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng id=" + orderId));

        // Không kiểm tra quyền sở hữu -> ai đăng nhập cũng tạo được link
        // thanh toán cho ĐƠN CỦA NGƯỜI KHÁC (không mất tiền oan vì tiền vẫn
        // phải trả thật, nhưng có thể bị lợi dụng để thăm dò/phá rối).
        if (!order.getUser().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("Bạn không có quyền thanh toán đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // Tự sinh txnRef MỚI mỗi lần gọi — không dùng thẳng orderId, vì user
        // có thể bấm "Thanh toán lại" nhiều lần cho cùng 1 đơn (lần đầu bỏ
        // dở, timeout...). Mỗi lần thử là 1 Payment record riêng.
        String txnRef = orderId + "-" + System.currentTimeMillis();
        Payment payment = new Payment(order, "VNPAY", txnRef, order.getTotalAmount());
        paymentRepository.save(payment);

        // amount lấy TỪ order.getTotalAmount() — server tự tính từ dữ liệu
        // ĐÃ LƯU ở Lesson 0018, không phải từ tham số client gửi lên. Xem
        // nguyên tắc số 1 ở đầu lesson.
        long amountVnd = order.getTotalAmount().longValueExact();
        return vnPayService.createPaymentUrl(txnRef, amountVnd,
                "Thanh toan don hang " + orderId, ipAddress);
    }

    @Transactional
    public void handleIpnCallback(Map<String, String> params) {
        if (!vnPayService.verifySignature(params)) {
            throw new IllegalStateException("Chữ ký không hợp lệ — dữ liệu có thể đã bị can thiệp");
        }

        String txnRef = params.get("vnp_TxnRef");
        Payment payment = paymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch txnRef=" + txnRef));

        // Idempotency — xem nguyên tắc số 3 ở đầu lesson. Đã SUCCESS rồi thì
        // dừng ngay, VNPay gọi lại bao nhiêu lần cũng không xử lý thêm lần nào.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        // Đối soát: số tiền trong callback PHẢI khớp số tiền đã lưu lúc tạo
        // giao dịch (payment.getAmount()) — không tự dưng tin callback nói gì
        // cũng đúng. Lệch số tiền là dấu hiệu bất thường cần chặn lại, không
        // âm thầm bỏ qua.
        long callbackAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
        if (callbackAmount != payment.getAmount().longValueExact()) {
            throw new IllegalStateException("Số tiền callback không khớp với giao dịch đã tạo");
        }

        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            payment.markSuccess(params.get("vnp_TransactionNo"));
            payment.getOrder().markPaid(); // state machine đã viết ở Lesson 0018
        } else {
            payment.markFailed();
        }
        paymentRepository.save(payment);
    }
}