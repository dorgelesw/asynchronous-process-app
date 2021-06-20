package com.providus.tech.restfulservice.controller;

import com.providus.tech.restfulservice.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/reactive-type")
public class AsyncReactiveTypeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskService taskService;

    @GetMapping("/block-mono")
    public Mono<String> reactiveBlockingTaskMono() {
        logger.info("reactive-mono-return-type: Request received");
        try {
            logger.info("reactive-mono-return-type: Task in progress");
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("reactive-mono-return-type: Servlet thread released");
        return Mono.just("Task finished");
    }

    @GetMapping("/async-mono")
    public DeferredResult<Mono<String>> reactiveAsyncTaskMono() {
        logger.info("reactive-async-mono-return-type: Request received");
        DeferredResult<Mono<String>> deferredResult = new DeferredResult<>();

        CompletableFuture.supplyAsync(taskService::execute)
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(Mono.just(result)));
        deferredResult.onCompletion(() -> logger.info("onCompletion: Processing complete"));
        deferredResult.onTimeout(() -> {
            logger.info("onTimeout: callback function deferredResult.onTimeout()");
            deferredResult.setErrorResult("Request timeout occurred.");
        });
        logger.info("reactive-async-mono-return-type: Servlet thread released");
        return deferredResult;
    }

    @GetMapping(value = "/json-flux", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> reactiveJsonBlockingTaskFlux() {
        logger.info("reactive-json-flux-return-type: Request received");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("reactive-json-flux-return-type: Servlet thread released");
        return Flux.fromIterable(list);
    }

    @GetMapping(value = "/stream-flux", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<String> reactiveBlockingTaskFlux() {
        logger.info("reactive-stream-flux-return-type: Request received");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("reactive-stream-flux-return-type: Servlet thread released");
        return Flux.fromIterable(list);
    }

}
