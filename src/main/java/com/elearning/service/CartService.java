package com.elearning.service;

import com.elearning.dto.CartItemResponse;
import com.elearning.dto.CartResponse;
import com.elearning.exception.NotFoundException;
import com.elearning.model.Course;
import com.elearning.repository.CourseRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class CartService {

    private final CourseRepository courseRepository;
    private final StringRedisTemplate redisTemplate;
    private static final Duration CART_TTL = Duration.ofDays(30);

    public CartService(CourseRepository courseRepository, StringRedisTemplate redisTemplate) {
        this.courseRepository = courseRepository;
        this.redisTemplate = redisTemplate;
    }
    private String keyFor(String email) {
        return "cart:" + email;
    }

//    public  void addItem (String email, Long courseId, int quantity)  {
//        if(!courseRepository.existsById(courseId)) {
//            throw new NotFoundException("Không tìm thấy khoá học id=" + courseId);
//        }
//
//        String key = keyFor(email);
//        redisTemplate.opsForHash().increment(key, courseId.toString(), quantity);
//        touchTtl(key);
//    }
public void addItem(String email, Long courseId, int quantity) {
    if (!courseRepository.existsById(courseId)) {
        throw new NotFoundException("Không tìm thấy khoá học id=" + courseId);
    }

    String key = keyFor(email);
    String field = courseId.toString();

    // "Ngây thơ": tách thành 3 bước riêng — GET, tính ở Java, rồi SET (khác
    // HINCRBY chỉ 1 lệnh). Giữa bước đọc và bước ghi có 1 khoảng hở thời
    // gian (network round-trip + xử lý ở app) — request khác hoàn toàn có
    // thể chen vào giữa khoảng hở đó.
    Object currentValue = redisTemplate.opsForHash().get(key, field);
    int currentQuantity = currentValue != null ? Integer.parseInt((String) currentValue) : 0;

    // Cố tình delay để "khoảng hở" đủ rộng cho race condition LỘ RA CHẮC
    // CHẮN khi test — localhost round-trip quá nhanh (dưới 1ms) nên nếu
    // không có dòng này, đôi khi 20 request vẫn tình cờ ra đúng 20 (race
    // condition không sai 100% lần nào, đó mới là lý do nó nguy hiểm trong
    // thực tế — bug không tái hiện đều mỗi lần chạy). XOÁ dòng sleep này sau
    // khi test xong.
//    try {
//        Thread.sleep(20);
//    } catch (InterruptedException e) {
//        Thread.currentThread().interrupt();
//    }

    int newQuantity = currentQuantity + quantity;
    redisTemplate.opsForHash().put(key, field, String.valueOf(newQuantity));
    touchTtl(key);
}

    private void touchTtl(String key) {
        redisTemplate.expire(key, CART_TTL);
    }

    public void updateItemQuantity(String email, Long courseId, int quantity) {
        if (quantity <= 0) {
            removeItem(email, courseId);
            return;
        }
        String key = keyFor(email);
        // HSET — set THẲNG thành giá trị tuyệt đối ("sửa thành đúng số lượng
        // X"), khác addItem (cộng dồn). Xem callout phía trên.
        redisTemplate.opsForHash().put(key, courseId.toString(), String.valueOf(quantity));
        touchTtl(key);
    }
    public void removeItem(String email, Long courseId) {
        redisTemplate.opsForHash().delete(keyFor(email), courseId.toString());
    }
    public void clearCart(String email) {
        redisTemplate.delete(keyFor(email));
    }
    public CartResponse getCart(String email) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(keyFor(email));
        if (raw.isEmpty()) {
            return new CartResponse(List.of(), BigDecimal.ZERO);
        }

        List<Long> courseIds = raw.keySet().stream().map(k -> Long.valueOf((String) k)).toList();
        Map<Long, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            Long courseId = Long.valueOf((String) entry.getKey());
            Course course = courseMap.get(courseId);
            // Course có thể đã bị xoá SAU KHI được thêm vào giỏ (Redis không
            // biết gì về việc đó — không có foreign key giữa 2 nguồn dữ liệu
            // khác nhau). Bỏ qua item mồ côi thay vì crash cả giỏ hàng; dọn
            // rác này để sau (chưa cần ở quy mô hiện tại — YAGNI).
            if (course == null) continue;

            int quantity = Integer.parseInt((String) entry.getValue());
            BigDecimal subtotal = course.getPrice().multiply(BigDecimal.valueOf(quantity));
            items.add(new CartItemResponse(course.getId(), course.getTitle(), course.getSlug(),
                    course.getThumbnailUrl(), course.getPrice(), quantity, subtotal));
            total = total.add(subtotal);
        }
        return new CartResponse(items, total);
    }


}
