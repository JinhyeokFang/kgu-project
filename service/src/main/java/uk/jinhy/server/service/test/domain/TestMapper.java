package uk.jinhy.server.service.test.domain;

import org.mapstruct.Mapper;
import uk.jinhy.server.api.domain.Test;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface TestMapper {
    TestEntity toEntity(Test test);
    Test toPojo(TestEntity testEntity);
}
