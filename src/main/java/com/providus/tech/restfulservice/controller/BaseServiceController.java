package com.providus.tech.restfulservice.controller;

import com.providus.tech.restfulservice.controller.response.ComputeResponse;
import com.providus.tech.restfulservice.repository.DataEngine;
import com.providus.tech.restfulservice.repository.DataEngineRepository;
import com.providus.tech.restfulservice.service.AsyncServiceEngine;
import com.providus.tech.restfulservice.service.BaseServiceEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;

@RestController
public class BaseServiceController {

    public static Collection<Integer> computeNaturalNumberInProgress = new HashSet<>();

    @Autowired
    BaseServiceEngine baseServiceEngine;

    @Autowired
    AsyncServiceEngine asyncServiceEngine;

    @Autowired
    private DataEngineRepository dataEngineRepository;

    @GetMapping("/base-service")
    public ComputeResponse baseService(@RequestParam(value = "naturalNumber") int naturalNumber) {
        return baseServiceEngine.computeService(Integer.valueOf(naturalNumber));
    }

    @GetMapping("/async-service")
    public ComputeResponse asyncService(@RequestParam(value = "naturalNumber") int naturalNumber) {
        Integer number = Integer.valueOf(naturalNumber);

        // Don't allow to start computation for the same natural number that computation is in progress.
        synchronized (computeNaturalNumberInProgress) {
            if (computeNaturalNumberInProgress.contains(naturalNumber)) {
                return new ComputeResponse(naturalNumber);
            }
        }

        // retrieve existing computation result for a giving natural number.
        if (dataEngineRepository.existsByNaturalNumber(naturalNumber)) {
            DataEngine dataEngine = dataEngineRepository.findByNaturalNumber(naturalNumber);
            return new ComputeResponse(dataEngine.getNaturalNumber(), responseMessage(dataEngine.getComputeNumber()));
        }

        synchronized (computeNaturalNumberInProgress) {
            computeNaturalNumberInProgress.add(naturalNumber);
        }

        // compute the natural number into another thread
        asyncServiceEngine.asynceService(naturalNumber);


        // return default message without wait for the end of the computation
        return new ComputeResponse(naturalNumber);
    }

    public static String responseMessage(int computeNumber) {
        return String.format("The generated number is %d", computeNumber);
    }
}
