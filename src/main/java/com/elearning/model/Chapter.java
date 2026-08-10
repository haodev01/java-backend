package com.elearning.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "chapter_order", nullable = false)
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lesson> lessons = new ArrayList<>();

    protected Chapter() {}

    public Chapter(String title, int order) {
        this.title = title;
        this.order = order;
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        lesson.setChapter(this);
    }

    // package-private — CHỦ Ý không có getCourse() công khai, xem callout dưới
    void setCourse(Course course) { this.course = course; }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getOrder() { return order; }
    public List<Lesson> getLessons() { return lessons; }
}
