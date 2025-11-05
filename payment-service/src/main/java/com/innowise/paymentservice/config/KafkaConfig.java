package com.innowise.paymentservice.config;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaConfig {

  private List<TopicConfig> topics;

  @Bean
  public List<NewTopic> topics() {
    return topics.stream()
        .map(topicCgf -> new NewTopic(topicCgf.getName(), topicCgf.getPartitions(), topicCgf.getReplicationFactor()))
        .toList();
  }

  @Getter
  @Setter
  public static class TopicConfig {
    private String name;
    private int partitions;
    private short replicationFactor;

  }

}
