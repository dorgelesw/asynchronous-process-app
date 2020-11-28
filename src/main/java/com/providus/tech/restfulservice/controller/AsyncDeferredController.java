package com.providus.tech.restfulservice.controller;

import com.providus.tech.restfulservice.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/deferred")
public class AsyncDeferredController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TaskService taskService;

    @Autowired
    public AsyncDeferredController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/completion")
    public DeferredResult<String> executeTask() {
        logger.info("Request received");
        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(taskService::execute)
                .whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
        logger.info("Servlet thread released");

        return deferredResult;
    }
}
