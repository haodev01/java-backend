package com.elearning.logging;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SensitiveDataMasker {

    // Jackson 3 (Spring Boot 4.1): package đổi từ com.fasterxml.jackson.* sang
    // tools.jackson.* — và ObjectMapper thay bằng JsonMapper dựng qua builder bất
    // biến (immutable), an toàn dùng chung giữa nhiều thread mà không cần đồng bộ.
    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    private static final String MASK = "***";
//    "password", "oldpassword", "newpassword", "token", "accesstoken", "refreshtoken"
    private static final Set<String> SENSITIVE_FIELDS = Set.of();
//    "authorization", "cookie"
    private static final Set<String> SENSITIVE_HEADERS = Set.of();

    public static Map<String, String> maskHeaders(Map<String, String> headers) {
        return headers.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> SENSITIVE_HEADERS.contains(e.getKey().toLowerCase()) ? MASK : e.getValue()
        ));
    }

    public static Object maskJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return "";
        try {
            JsonNode node = jsonMapper.readTree(rawJson);
            maskNode(node);
            return node;
        } catch (Exception e) {
            // Không phải JSON hợp lệ (form data, body rỗng...) — trả nguyên văn.
            return rawJson;
        }
    }

    private static void maskNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            Iterator<String> fieldNames = obj.propertyNames().iterator();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                if (SENSITIVE_FIELDS.contains(field.toLowerCase())) {
                    obj.put(field, MASK);
                } else {
                    maskNode(obj.get(field));
                }
            }
        } else if (node.isArray()) {
            node.forEach(SensitiveDataMasker::maskNode);
        }
    }
}
