package com.elearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected Progress() {}

    public Progress(Enrollment enrollment, Lesson lesson) {
        this.enrollment = enrollment;
        this.lesson = lesson;
        this.completed = false;
    }

    public void markComplete() {
        this.completed = true;
        this.completedAt = Instant.now();
    }

    @JsonIgnore
    public Enrollment getEnrollment() { return enrollment; }

    public Long getId() { return id; }
    public Lesson getLesson() { return lesson; }
    public boolean isCompleted() { return completed; }
    public Instant getCompletedAt() { return completedAt; }
}
