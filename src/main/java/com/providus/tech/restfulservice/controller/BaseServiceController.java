package com.providus.tech.restfulservice.controller;

import com.providus.tech.restfulservice.controller.response.ComputeResponse;
import com.providus.tech.restfulservice.service.AsyncServiceEngine;
import com.providus.tech.restfulservice.service.BaseServiceEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseServiceController {

    @Autowired
    BaseServiceEngine baseServiceEngine;

    @Autowired
    AsyncServiceEngine asyncServiceEngine;

    @GetMapping("/base-service")
    public ComputeResponse baseService(@RequestParam(value = "naturalNumber") int naturalNumber) {
        return baseServiceEngine.computeService(Integer.valueOf(naturalNumber));
    }

    @GetMapping("/async-service")
    public ComputeResponse asyncService(@RequestParam(value = "naturalNumber") int naturalNumber) {
        return asyncServiceEngine.asynceService(Integer.valueOf(naturalNumber));
    }
}
