package com.elearning.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        // Constructor 1-tham số (new GenericJacksonJsonRedisSerializer(mapper))
        // KHÔNG tự bật "default typing" — JSON lưu vào Redis không có thông tin
        // kiểu (@class), nên lúc đọc lại Jackson chỉ dựng được LinkedHashMap
        // chung chung, ném ClassCastException khi ép về PageResponse/CourseSummary.
        // Phải dùng builder() + enableDefaultTyping() để nhúng kèm thông tin kiểu.
        var typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.elearning.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.math.")
                .allowIfSubType("java.time.")
                .build();
        var jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .build();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // TTL cứng — dù quên evict chỗ nào đó, cache tự hết hạn sau 5 phút
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        return builder -> builder.cacheDefaults(config);
    }
}
