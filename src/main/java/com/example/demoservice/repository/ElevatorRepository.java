package com.example.demoservice.repository;

import com.example.demoservice.model.Elevator;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevatorRepository extends CrudRepository<Elevator, Integer> {
}
