#!/bin/bash
# Dev-only: tạo nhiều khoá học mẫu qua API THẬT (không đụng migration Flyway) —
# để test N+1 (Lesson 0012/0013), pagination, cache (lesson sau) có ý nghĩa thật.
# KHÔNG chạy script này nhắm vào production.
#
# Cách dùng:
#   ./scripts/seed-courses.sh <ACCESS_TOKEN_CUA_INSTRUCTOR_HOAC_ADMIN> [so_luong=15]

set -euo pipefail

TOKEN="${1:?Cách dùng: ./scripts/seed-courses.sh <ACCESS_TOKEN> [số lượng]}"
COUNT="${2:-15}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

for i in $(seq 1 "$COUNT"); do
  status=$(curl -s -o /tmp/seed-course-response.json -w "%{http_code}" \
    -X POST "$BASE_URL/api/v1/courses" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"Khoá học mẫu số ${i}\",
      \"slug\": \"khoa-hoc-mau-${i}-$(date +%s%N)\",
      \"description\": \"Mô tả khoá học mẫu số ${i}\",
      \"price\": $((i * 50000)),
      \"chapters\": [
        {
          \"title\": \"Chương 1: Nhập môn\",
          \"order\": 1,
          \"lessons\": [
            {\"title\": \"Bài 1\", \"order\": 1, \"contentType\": \"VIDEO\"},
            {\"title\": \"Bài 2\", \"order\": 2, \"contentType\": \"VIDEO\"}
          ]
        },
        {
          \"title\": \"Chương 2: Nâng cao\",
          \"order\": 2,
          \"lessons\": [
            {\"title\": \"Bài 1\", \"order\": 1, \"contentType\": \"ARTICLE\"}
          ]
        }
      ]
    }")

  if [ "$status" = "200" ]; then
    echo "[$i/$COUNT] OK — Khoá học mẫu số $i"
  else
    echo "[$i/$COUNT] LỖI (HTTP $status):"
    cat /tmp/seed-course-response.json
    echo
  fi
done

echo "Xong. Kiểm tra: curl $BASE_URL/api/v1/courses | wc -c"
