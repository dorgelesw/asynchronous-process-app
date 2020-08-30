package com.providus.tech.restfulservice.service;

import com.providus.tech.restfulservice.repository.DataEngine;
import com.providus.tech.restfulservice.repository.DataEngineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.providus.tech.restfulservice.service.BaseServiceEngine.computeNaturalNumberInProgress;

@Component
@Scope("prototype")
public class ComputeTask implements Runnable {

    private static final InheritableThreadLocal<Integer> naturalNumber = new InheritableThreadLocal<>();

    @Autowired
    private DataEngineRepository dataEngineRepository;

    /**
     * Use the formula randomNumber * (max - min + 1) + min to generate values with the min value inclusive and the max exclusive.
     * @return a random number or fail random number in case of InterruptedException.
     */
    @Override
    public void run() {
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
            dataEngineRepository.save(new DataEngine(ComputeTask.naturalNumber.get().intValue(), computeNumber));
            BaseServiceEngine.computeNaturalNumberInProgress.remove(ComputeTask.naturalNumber.get().intValue());
            ComputeTask.naturalNumber.remove();
        }
    }

    public static void setNaturalNumber(Integer naturalNumber) {
        ComputeTask.naturalNumber.set(naturalNumber);
    }
}
