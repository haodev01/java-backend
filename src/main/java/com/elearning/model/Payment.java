package com.elearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String provider;

    // Mã giao dịch DO MÌNH TỰ SINH, duy nhất cho mỗi lần thử thanh toán —
    // khác providerTxnId (VNPay tự sinh, chỉ có SAU khi thanh toán xong).
    // Dùng txnRef để đối chiếu khi IPN gọi về, không dùng orderId trực tiếp
    // vì 1 Order có thể có NHIỀU lần thử thanh toán (lần đầu thất bại, thử lại).
    @Column(name = "txn_ref", nullable = false, unique = true)
    private String txnRef;

    @Column(name = "provider_txn_id")
    private String providerTxnId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "paid_at")
    private Instant paidAt;

    protected Payment() {}

    public Payment(Order order, String provider, String txnRef, BigDecimal amount) {
        this.order = order;
        this.provider = provider;
        this.txnRef = txnRef;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public void markSuccess(String providerTxnId) {
        this.status = PaymentStatus.SUCCESS;
        this.providerTxnId = providerTxnId;
        this.paidAt = Instant.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    // @JsonIgnore ngay từ đầu — tránh lặp lại đúng bug vòng lặp serialize
    // (Order <-> OrderItem) đã tự debug ở Lesson 0018, lần này là Payment -> Order.
    @JsonIgnore
    public Order getOrder() { return order; }

    public Long getId() { return id; }
    public String getProvider() { return provider; }
    public String getTxnRef() { return txnRef; }
    public String getProviderTxnId() { return providerTxnId; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public Instant getPaidAt() { return paidAt; }
}