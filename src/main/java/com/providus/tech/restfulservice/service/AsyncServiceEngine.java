package com.providus.tech.restfulservice.service;

import com.providus.tech.restfulservice.controller.response.ComputeResponse;
import com.providus.tech.restfulservice.repository.DataEngine;
import com.providus.tech.restfulservice.repository.DataEngineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Service
public class AsyncServiceEngine {

    public Collection<Integer> computeNaturalNumberInProgress = new HashSet<>();

    @Autowired
    private DataEngineRepository dataEngineRepository;

    public ComputeResponse asynceService(Integer naturalNumber) {

        // Don't allow to start computation for the same natural number that computation is in progress.
        synchronized (computeNaturalNumberInProgress) {
            if (computeNaturalNumberInProgress.contains(naturalNumber)) {
                return new ComputeResponse(naturalNumber.intValue());
            }
        }

        // retrieve existing computation result for a giving natural number.
        if (dataEngineRepository.existsByNaturalNumber(naturalNumber.intValue())) {
            DataEngine dataEngine = dataEngineRepository.findByNaturalNumber(naturalNumber.intValue());
            return new ComputeResponse(dataEngine.getNaturalNumber(), responseMessage(dataEngine.getComputeNumber()));
        }

        synchronized (computeNaturalNumberInProgress) {
            computeNaturalNumberInProgress.add(naturalNumber);
        }

        // compute the natural number into another thread
        computeTask(naturalNumber);

        // return default message without wait for the end of the computation
        return new ComputeResponse(naturalNumber.intValue());
    }

    @Async
    @Transactional
    public void computeTask(Integer naturalNumber) {
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
            dataEngineRepository.save(new DataEngine(naturalNumber.intValue(), computeNumber));
            computeNaturalNumberInProgress.remove(naturalNumber);
        }
    }

    private String responseMessage(int computeNumber) {
        return String.format("The generated number is %d", computeNumber);
    }
}
