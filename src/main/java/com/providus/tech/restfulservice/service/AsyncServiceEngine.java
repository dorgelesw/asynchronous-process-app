package com.providus.tech.restfulservice.service;

import com.providus.tech.restfulservice.repository.DataEngine;
import com.providus.tech.restfulservice.repository.DataEngineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.providus.tech.restfulservice.controller.BaseServiceController.computeNaturalNumberInProgress;

@Service
public class AsyncServiceEngine {

    @Autowired
    private DataEngineRepository dataEngineRepository;

    @Async
    @Transactional
    public void asynceService(int naturalNumber) {

        int computeNumber;
        int min = 1;
        int max = 1001;
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
            computeNumber = 99;
        }
        computeNumber =  (int)(Math.random() * (max - min + 1) + min);

        // computation not fail. Save computation result
        if (computeNumber != 99) {
            dataEngineRepository.save(new DataEngine(naturalNumber, computeNumber));
        }
        computeNaturalNumberInProgress.remove(naturalNumber);

    }
}
