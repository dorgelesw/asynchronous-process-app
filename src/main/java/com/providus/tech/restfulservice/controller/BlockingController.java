package com.providus.tech.restfulservice.controller;

import com.providus.tech.restfulservice.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockingController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TaskService taskService;


    @Autowired
    public BlockingController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/blockingTask")
    public String executeTask() {
        logger.info("Request received");
        String result = taskService.execute();
        logger.info("Servlet thread released");

        return result;
    }
}
