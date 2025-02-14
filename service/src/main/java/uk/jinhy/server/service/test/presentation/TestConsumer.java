package uk.jinhy.server.service.test.presentation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uk.jinhy.server.api.domain.Test;
import uk.jinhy.server.service.test.domain.TestEntity;
import uk.jinhy.server.service.test.domain.TestMapper;
import uk.jinhy.server.service.test.domain.TestRepository;

@Slf4j
@Component
@RequiredArgsConstructor
class TestConsumer {
    private final TestRepository testRepository;
    private final TestMapper testMapper;

    @KafkaListener(topics = "test-write-topic")
    public void processWrite(String message) {
        try {
            Test test = Test.builder().id(null).message(message).build();
            TestEntity testEntity = testMapper.toEntity(test);
            testRepository.save(testEntity);
            log.info("Entity saved");
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
