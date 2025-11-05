package com.innowise.paymentservice.config;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaConfig {

  private List<TopicConfig> topics;

  @Bean
  public KafkaAdmin.NewTopics topics() {
    return new NewTopics(
        topics.stream()
            .map(topicCgf -> TopicBuilder.name(topicCgf.getName())
                .partitions(topicCgf.getPartitions())
                .replicas(topicCgf.getReplicationFactor())
                .build()
            )
            .toArray(NewTopic[]::new)
    );
  }

  @Getter
  @Setter
  private static class TopicConfig {

    private String name;
    private int partitions;
    private short replicationFactor;

  }

}
