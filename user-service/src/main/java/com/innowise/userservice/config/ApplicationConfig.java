package com.innowise.userservice.config;

import java.util.ArrayList;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
@EnableCaching
public class ApplicationConfig {

  @Bean
  public PolymorphicTypeValidator polymorphicTypeValidator() {
    return BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType("com.innowise.userservice.model.dto.")
        .allowIfSubType("com.innowise.userservice.model.dto.")
        .allowIfSubType(ArrayList.class)
        .build();
  }

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration(PolymorphicTypeValidator validator) {
    return RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues()
        .serializeKeysWith(
            SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            SerializationPair.fromSerializer(
                GenericJacksonJsonRedisSerializer.builder()
                    .enableDefaultTyping(validator)
                    .build()
            )
        );
  }

}
