package com.example.demoservice.controller;

import com.example.demoservice.model.ElevatorStatus;

import javax.validation.constraints.NotNull;

public class SummonRequestDto {
    @NotNull
    private ElevatorStatus status;

    public ElevatorStatus getStatus() {
        return status;
    }

    public void setStatus(ElevatorStatus status) {
        this.status = status;
    }
}
