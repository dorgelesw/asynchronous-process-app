package com.providus.tech.restfulservice.service;

import com.providus.tech.restfulservice.controller.response.ComputeResponse;
import com.providus.tech.restfulservice.repository.DataEngine;
import com.providus.tech.restfulservice.repository.DataEngineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import static com.providus.tech.restfulservice.controller.BaseServiceController.computeNaturalNumberInProgress;
import static com.providus.tech.restfulservice.controller.BaseServiceController.responseMessage;

@Service
public class BaseServiceEngine {

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataEngineRepository dataEngineRepository;

    public ComputeResponse computeService(Integer naturalNumber) {

        ComputeTask computeTask = applicationContext.getBean(ComputeTask.class);

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
            //set ThreadLocal context
            computeNaturalNumberInProgress.add(naturalNumber);
            ComputeTask.setNaturalNumber(naturalNumber);
        }

        // compute the natural number into another thread
        taskExecutor.execute(computeTask);


        // return default message without wait for the end of the computation
        return new ComputeResponse(naturalNumber.intValue());
    }
}
