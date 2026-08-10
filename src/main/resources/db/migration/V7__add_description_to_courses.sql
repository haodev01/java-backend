-- Thiếu sót thật từ V6 — mọi khoá học cần mô tả, không riêng gì thumbnail/video
-- (2 field đó cố ý hoãn sang lesson upload file riêng, xem roadmap Phase 2).
ALTER TABLE courses
    ADD COLUMN description TEXT NULL;
