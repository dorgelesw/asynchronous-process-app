package com.providus.tech.restfulservice.controller;

import com.providus.tech.restfulservice.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/deferred")
public class AsyncDeferredController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TaskService taskService;

    @Autowired
    public AsyncDeferredController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/onCompletion")
    public DeferredResult<String> executeTaskOnCompletion() {
        logger.info("onCompletion: Request received");
        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(taskService::execute)
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
        logger.info("onCompletion: Servlet thread released");
        deferredResult.onCompletion(() -> logger.info("onCompletion: Processing complete"));
        return deferredResult;
    }

    @GetMapping("/onTimeout")
    public DeferredResult<String> executeTaskOnTimeout() {
        logger.info("onTimeout: Request received");
        DeferredResult<String> deferredResult = new DeferredResult<>(6000l);
        CompletableFuture.supplyAsync(taskService::execute)
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
        logger.info("onTimeout: Servlet thread released");
        deferredResult.onCompletion(() -> logger.info("onTimeout: Processing complete"));
        deferredResult.onTimeout(() -> deferredResult.setErrorResult("Request timeout occurred."));
        return deferredResult;
    }

    @GetMapping("/onError")
    public DeferredResult<String> executeTaskOnError() {
        logger.info("onError: Request received");
        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
                throw new RuntimeException();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Task finished";
            })
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
        logger.info("onError: Servlet thread released");
        deferredResult.onCompletion(() -> logger.info("onError: Processing complete"));
        deferredResult.onError(throwable -> deferredResult.setErrorResult("An error occurred."));
        return deferredResult;
    }
}
