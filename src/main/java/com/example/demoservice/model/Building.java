package com.example.demoservice.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "buildings")
public class Building {
    @Id
    @Column(name = "buildingId")
    @GeneratedValue
    private Integer id;
    @NotBlank
    private String name;
    @NotBlank
    private String location;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "building_has_elevators", joinColumns = { @JoinColumn(name = "buildingId", referencedColumnName = "buildingId") }, inverseJoinColumns = { @JoinColumn(name = "elevatorId", referencedColumnName = "elevatorId") })
    private Set<Elevator> elevators = new HashSet();

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<Elevator> getElevators() {
        return elevators;
    }

    public void setElevators(Set<Elevator> elevators) {
        this.elevators = elevators;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Building building = (Building) o;
        return Objects.equals(id, building.id) && name.equals(building.name) && location.equals(building.location) && elevators.equals(building.elevators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location, elevators);
    }
}
