package com.elearning.service;

import com.elearning.dto.OrderPaidEvent;
import com.elearning.exception.NotFoundException;
import com.elearning.model.Enrollment;
import com.elearning.model.Order;
import com.elearning.model.OrderItem;
import com.elearning.model.User;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, OrderRepository orderRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.orderRepository = orderRepository;
    }

    // AFTER_COMMIT — xem callout phía trên. PaymentService KHÔNG hề gọi
    // method này trực tiếp — Spring tự tìm và gọi khi thấy OrderPaidEvent
    // được publish, sau khi transaction publish nó đã commit xong.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng id=" + event.orderId()));
        User user = order.getUser();

        for (OrderItem item : order.getItems()) {
            // 1 Order có thể có nhiều OrderItem cùng courseId nếu user mua
            // lại đúng course đã sở hữu (hiếm nhưng không cấm ở Cart/Order) —
            // existsByUserAndCourse tránh ghi danh trùng, enrollments có UNIQUE
            // constraint (user_id, course_id) làm lớp chặn cuối nếu lỡ vượt qua.
            if (!enrollmentRepository.existsByUserAndCourse(user, item.getCourse())) {
                enrollmentRepository.save(new Enrollment(user, item.getCourse()));
            }
        }
    }

    public List<Enrollment> myEnrollments(String email) {
        // Rút gọn — thực tế nên lấy User qua UserService.getByEmail() như
        // các Service khác, ở đây bỏ qua để tập trung vào phần chính lesson.
        return enrollmentRepository.findByUser(
                orderRepository.findAll().stream()
                        .filter(o -> o.getUser().getEmail().equals(email))
                        .findFirst().map(Order::getUser)
                        .orElseThrow(() -> new NotFoundException("Không có dữ liệu")));
    }
}