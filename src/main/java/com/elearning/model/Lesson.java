package com.elearning.model;

import com.elearning.model.Chapter;
import jakarta.persistence.*;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "lesson_order", nullable = false)
    private int order;

    @Column(name = "content_type", nullable = false)
    private String contentType; // "VIDEO" | "ARTICLE"... đơn giản hoá, chưa tách enum riêng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    protected Lesson() {}

    public Lesson(String title, int order, String contentType) {
        this.title = title;
        this.order = order;
        this.contentType = contentType;
    }

    void setChapter(Chapter chapter) { this.chapter = chapter; } // package-private, lý do như Chapter.setCourse()

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getOrder() { return order; }
    public String getContentType() { return contentType; }
}
