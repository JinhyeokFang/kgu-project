package uk.jinhy.server.service.test.presentation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import uk.jinhy.server.api.domain.Test;
import uk.jinhy.server.service.test.domain.TestMapper;
import uk.jinhy.server.service.test.domain.TestRepository;

import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final TestRepository testRepository;
    private final TestMapper testMapper;

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_PAGE_KEY = "test:page:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(10);
    private static final String KAFKA_TOPIC = "test-write-topic";

    @GetMapping
    public ResponseEntity<List<Test>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String redisKey = REDIS_PAGE_KEY + page + ":" + size;
        Object cachedValue = redisTemplate.opsForValue().get(redisKey);

        if (cachedValue instanceof String cachedJson) {
            try {
                List<Test> cachedPage = objectMapper.readValue(cachedJson, new TypeReference<>() {});
                log.info("Cache hit for page: {}", page);
                return ResponseEntity.ok(cachedPage);
            } catch (Exception e) {
                log.error("Cache deserialization failed, falling back to DB", e);
            }
        }

        List<Test> entities = testRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        ).map(testMapper::toPojo).toList();

        try {
            String json = objectMapper.writeValueAsString(entities);
            redisTemplate.opsForValue().set(redisKey, json, CACHE_TTL);
        } catch (Exception e) {
            log.error("Cache serialization failed", e);
        }

        return ResponseEntity.ok(entities);
    }

    @PostMapping
    public ResponseEntity<String> create() {
        kafkaTemplate.send(KAFKA_TOPIC, "test");
        log.info("Message sent to Kafka");
        return ResponseEntity.ok("Message queued for processing");
    }
}
