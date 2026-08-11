package com.elearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Snapshot giá TẠI THỜI ĐIỂM MUA — xem callout ở mục 1 để hiểu vì sao
    // không join sang Course.price khi hiển thị đơn hàng cũ.
    @Column(name = "price_at_purchase", nullable = false)
    private BigDecimal priceAtPurchase;

    protected OrderItem() {}

    public OrderItem(Course course, BigDecimal priceAtPurchase) {
        this.course = course;
        this.priceAtPurchase = priceAtPurchase;
    }

    void setOrder(Order order) { this.order = order; } // package-private, giống Chapter.setCourse()

    public Long getId() { return id; }

    @JsonIgnore
    public Order getOrder() { return order; }
    public Course getCourse() { return course; }
    public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
}