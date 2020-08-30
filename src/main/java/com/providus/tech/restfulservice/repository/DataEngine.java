package com.providus.tech.restfulservice.repository;

import javax.persistence.*;
import javax.xml.crypto.Data;

@Entity
public class DataEngine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private int naturalNumber;

    @Column
    private int computeNumber;

    public DataEngine() {

    }

    public DataEngine(int naturalNumber, int computeNumber) {
        this.naturalNumber = naturalNumber;
        this.computeNumber = computeNumber;
    }

    public Long getId() {
        return id;
    }

    public int getNaturalNumber() {
        return naturalNumber;
    }

    public int getComputeNumber() {
        return computeNumber;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNaturalNumber(int naturalNumber) {
        this.naturalNumber = naturalNumber;
    }

    public void setComputeNumber(int computeNumber) {
        this.computeNumber = computeNumber;
    }
}
