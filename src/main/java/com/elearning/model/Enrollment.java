package com.elearning.model;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "enrollments")
public class Enrollment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    protected Enrollment() {}

    public Enrollment(User user, Course course) {
        this.user = user;
        this.course = course;
        this.enrolledAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Course getCourse() { return course; }
    public Instant getEnrolledAt() { return enrolledAt; }
}
