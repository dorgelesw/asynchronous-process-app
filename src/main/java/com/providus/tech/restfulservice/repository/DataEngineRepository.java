package com.providus.tech.restfulservice.repository;

import org.springframework.data.repository.CrudRepository;

public interface DataEngineRepository extends CrudRepository<DataEngine, Long> {
    DataEngine findByNaturalNumber(int naturalNumber);

    Boolean existsByNaturalNumber(int naturalNumber);
}
