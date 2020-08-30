package com.providus.tech.restfulservice.controller.response;

public class ComputeResponse {

    public static final String DEFAULT_MESSAGE = "The answer is being calculated, please call again later..";

    private int naturalNumber;

    private String message;

    public ComputeResponse() {

    }

    public ComputeResponse(int naturalNumber) {
        this(naturalNumber, DEFAULT_MESSAGE);
    }

    public ComputeResponse(int naturalNumber, String message) {
        this.naturalNumber = naturalNumber;
        this.message = message;
    }

    public int getNaturalNumber() {
        return this.naturalNumber;
    }

    public String getMessage() {
        return this.message;
    }

}
