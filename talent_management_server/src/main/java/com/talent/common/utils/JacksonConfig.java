package com.talent.common.utils;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 统一处理时区
 */
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder
                .simpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}

