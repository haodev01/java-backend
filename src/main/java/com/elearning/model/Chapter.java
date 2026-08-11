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

    // Xoá khỏi collection do Chapter quản lý — bắt buộc phải qua đường này
    // (không phải gọi thẳng 1 repository.delete() nào đó) vì Lesson không có
    // repository riêng (nguyên tắc aggregate root). orphanRemoval=true trên
    // @OneToMany bên dưới tự sinh DELETE khi save() Chapter với lesson đã bị
    // remove khỏi list.
    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
    }

    public void update(String title, Integer order) {
        if (title != null) this.title = title;
        if (order != null) this.order = order;
    }

    void setCourse(Course course) { this.course = course; }

    // @JsonIgnore: dùng được trong Java code (vd kiểm tra ownership ở
    // CourseService) nhưng KHÔNG serialize ra JSON — nếu không, Course có
    // getChapters() + Chapter có getCourse() sẽ lặp vô hạn Course->Chapter->Course
    // khi trả response, crash StackOverflowError.
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Course getCourse() { return course; }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getOrder() { return order; }
    public List<Lesson> getLessons() { return lessons; }
}
