package com.example.demoservice.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "elevators")
public class Elevator implements Serializable {
    @Id
    @Column(name = "elevatorId")
    @GeneratedValue
    private Integer id;
    @NotBlank
    private String name;
    @NotEmpty
    @ElementCollection
    private List<Integer> floors = new ArrayList<>();
    @NotNull
    @Enumerated(EnumType.STRING)
    private ElevatorStatus status;
    @NotNull
    private int currentFloor;

    {
        status = ElevatorStatus.STOPPED;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getFloors() {
        return floors;
    }

    public void setFloors(List<Integer> floors) {
        this.floors = floors;
    }

    public ElevatorStatus getStatus() {
        return status;
    }

    public void setStatus(ElevatorStatus elevatorStatus) {
        this.status = elevatorStatus;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Elevator elevator = (Elevator) o;
        return currentFloor == elevator.currentFloor && Objects.equals(id, elevator.id) && name.equals(elevator.name) && floors.equals(elevator.floors) && status == elevator.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, floors, status, currentFloor);
    }
}
