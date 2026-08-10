-- TEXT chỉ chứa tối đa 65KB — không đủ khi log toàn bộ body/header thay vì cắt bớt.
-- LONGTEXT chứa được tới 4GB, đủ dư cho mọi request/response thực tế của module Auth.
ALTER TABLE request_logs
    MODIFY COLUMN request_headers LONGTEXT,
    MODIFY COLUMN request_body LONGTEXT,
    MODIFY COLUMN response_body LONGTEXT;
