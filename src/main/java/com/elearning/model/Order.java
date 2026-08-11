package com.elearning.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private  User user;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    public Order(User user) {
        this.user = user;
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        this.totalAmount = this.totalAmount.add(item.getPriceAtPurchase());
    }

    // State machine — validate NGAY TRONG entity, không phải if/else rải rác
    // ở Service. Entity là nơi duy nhất chịu trách nhiệm "trạng thái nào được
    // phép đi tới trạng thái nào", giống pattern ensureOwnerOrAdmin ở Course
    // nhưng cho invariant khác (chuyển trạng thái thay vì quyền sở hữu).
    public void markPaid() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ đơn PENDING mới chuyển được sang PAID, hiện tại: " + status);
        }
        this.status = OrderStatus.PAID;
    }

    public void complete() {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Chỉ đơn PAID mới chuyển được sang COMPLETED, hiện tại: " + status);
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Đơn hàng đã hoàn tất không thể huỷ");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
