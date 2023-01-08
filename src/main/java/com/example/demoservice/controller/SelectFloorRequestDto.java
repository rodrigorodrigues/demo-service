package com.example.demoservice.controller;

import javax.validation.constraints.NotNull;

public class SelectFloorRequestDto {
    @NotNull
    private Integer floor;

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }
}
