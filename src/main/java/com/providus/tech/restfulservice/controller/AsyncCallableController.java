package com.providus.tech.restfulservice.controller;

import com.providus.tech.restfulservice.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.concurrent.Callable;

@RestController
@RequestMapping("/callable")
public class AsyncCallableController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final long TIMEOUT = 3000L;
    private final TaskService taskService;

    @Autowired
    public AsyncCallableController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/sample")
    public Callable<String> executeTask() {
        logger.info("Request received");
        Callable<String> callable = taskService::execute;
        logger.info("Servlet thread released");

        return callable;
    }

    @GetMapping("/timeout-handling")
    public WebAsyncTask<String> callableTimeout() {
        logger.info("Request received");
        Callable<String> callable = () -> {
            boolean call= true;
            while (call) {

            }
            return "Callable result OK";
        };
        logger.info("Servlet thread released");
        WebAsyncTask<String> webAsyncTask = new WebAsyncTask(TIMEOUT, callable);
        webAsyncTask.onTimeout(() -> {
            logger.info("onTimeout: callback function webAsyncTask.onTimeout()");
            return "Request timeout occurred.";
        });

        return webAsyncTask;
    }
}
