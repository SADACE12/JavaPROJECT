package org.example.javaalmas20.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka event publisher — publishes domain events to topics.
 * Only activated when KafkaTemplate is available (Kafka enabled).
 */
@Slf4j
@Service
@ConditionalOnBean(KafkaTemplate.class)
public class EventPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisherService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json);
            log.info("Published event to topic={}, key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish event to topic={}, key={}: {}", topic, key, e.getMessage());
        }
    }

    public void publishUserEvent(String action, Map<String, Object> data) {
        publish("user-events", action, data);
    }

    public void publishAuditEvent(String action, Map<String, Object> data) {
        publish("audit-events", action, data);
    }
}
