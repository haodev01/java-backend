package com.elearning.service;


import com.elearning.dto.CartItemResponse;
import com.elearning.dto.CartResponse;
import com.elearning.exception.NotFoundException;
import com.elearning.model.Course;
import com.elearning.model.Order;
import com.elearning.model.OrderItem;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CourseRepository courseRepository,
                        UserService userService, CartService cartService) {
        this.orderRepository = orderRepository;
        this.courseRepository = courseRepository;
        this.userService = userService;
        this.cartService = cartService;
    }

    // @Transactional CHỈ bọc phần MySQL (tạo Order + OrderItem) — nếu 1 trong
    // các course trong giỏ đã bị xoá SAU KHI thêm vào giỏ (Lesson 0017 gọi
    // đây là "item mồ côi"), NotFoundException ném ra giữa vòng for sẽ làm
    // TOÀN BỘ transaction rollback — không có chuyện Order được lưu nửa vời
    // với thiếu OrderItem. Xem mục 3 để tự tay chứng minh điều này.
    @Transactional
    public Order checkout(String email) {
        CartResponse cart = cartService.getCart(email);
        if (cart.items().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống, không thể đặt hàng");
        }

        User user = userService.getByEmail(email);
        Order order = new Order(user);

        for (CartItemResponse item : cart.items()) {
            Course course = courseRepository.findById(item.courseId())
                    .orElseThrow(() -> new NotFoundException(
                            "Khoá học id=" + item.courseId() + " không còn tồn tại"));
            // Đọc LẠI course.getPrice() trực tiếp, KHÔNG dùng item.price() có
            // sẵn trong CartResponse — item.price() lấy từ lúc gọi GET /cart,
            // có thể đã cũ vài giây/phút. priceAtPurchase phải là giá ĐÚNG
            // NGAY LÚC checkout, không phải giá lúc xem giỏ hàng.
            order.addItem(new OrderItem(course, course.getPrice()));
        }

        Order saved = orderRepository.save(order);
        cartService.clearCart(email); // Redis — NGOÀI transaction, xem callout phía trên
        return saved;
    }

    public Order markPaid(Long orderId) {
        Order order = getOrder(orderId);
        order.markPaid();
        return orderRepository.save(order);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng id=" + id));
    }

    public List<Order> myOrders(String email) {
        User user = userService.getByEmail(email);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
