package com.elearning.model;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    // cascade ALL + orphanRemoval: Chapter không có vòng đời riêng ngoài Course
    // (mục 1) — lưu/xoá Course tự lưu/xoá theo mọi Chapter của nó.
    // FetchType.EAGER: CỐ Ý — đơn giản để chạy được ngay. Đây chính là nguyên
    // nhân sẽ gây N+1, chưa sửa ở lesson này — Lesson 0013 sẽ đo và sửa.
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Chapter> chapters = new ArrayList<>();

    protected Course() {}

    public Course(String title, String slug, BigDecimal price, User instructor) {
        this.title = title;
        this.slug = slug;
        this.price = price;
        this.instructor = instructor;
        this.status = CourseStatus.DRAFT;
    }

    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
        chapter.setCourse(this);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public BigDecimal getPrice() { return price; }
    public User getInstructor() { return instructor; }
    public CourseStatus getStatus() { return status; }
    public List<Chapter> getChapters() { return chapters; }

    public void attachThumbnail(String url) { this.thumbnailUrl = url; }
    public String getThumbnailUrl() { return thumbnailUrl; }

}
