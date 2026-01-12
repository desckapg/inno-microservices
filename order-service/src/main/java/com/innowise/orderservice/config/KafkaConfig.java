package com.innowise.orderservice.config;

import com.innowise.common.exception.ExternalApiException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Predicate;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties.Retry.Topic.Backoff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.util.backoff.BackOff;

@Configuration
@ConfigurationProperties("spring.kafka")
@Data
@NullMarked
public class KafkaConfig {

  private Map<String, TopicConfig> topics;
  private KafkaProperties.Retry retry;

  @Bean
  public KafkaAdmin.NewTopics topics() {
    return new NewTopics(
        topics.values().stream()
            .filter(TopicConfig::isCreate)
            .map(topicCgf -> TopicBuilder.name(topicCgf.getName())
                .partitions(topicCgf.getPartitions())
                .replicas(topicCgf.getReplicationFactor())
                .build()
            )
            .toArray(NewTopic[]::new)
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(consumerFactory);
    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
    return factory;
  }

  @Bean
  public RetryTopicConfiguration retryTopicConfiguration(
      KafkaTemplate<String, String> kafkaTemplate,
      ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory
  ) {
    return RetryTopicConfigurationBuilder
        .newInstance()
        .listenerFactory(concurrentKafkaListenerContainerFactory)
        .customBackoff(getBackOff(retry.getTopic().getBackoff()))
        .excludeTopics(topics.values().stream()
            .filter(TopicConfig::isCreate)
            .map(TopicConfig::getName)
            .toList()
        )
        .retryOn(DataAccessException.class)
        .retryOn(ExternalApiException.class)
        .create(kafkaTemplate);
  }

  private BackOff getBackOff(Backoff retryTopicBackoff) {
    PropertyMapper map = PropertyMapper.get();
    RetryPolicy.Builder builder = RetryPolicy.builder().maxRetries(Long.MAX_VALUE);
    map.from(retryTopicBackoff.getDelay()).to(builder::delay);
    map.from(retryTopicBackoff.getMaxDelay()).when(Predicate.not(Duration::isZero))
        .to(builder::maxDelay);
    map.from(retryTopicBackoff.getMultiplier()).to(builder::multiplier);
    map.from(retryTopicBackoff.getJitter()).when((Predicate.not(Duration::isZero)))
        .to(builder::jitter);
    return builder.build().getBackOff();
  }

  @Getter
  @Setter
  private static class TopicConfig {

    private String name;
    private int partitions;
    private short replicationFactor;
    private boolean create;

  }

}
