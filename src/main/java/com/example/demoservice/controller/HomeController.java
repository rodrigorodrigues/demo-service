package com.example.demoservice.controller;

import com.example.demoservice.model.Building;
import com.example.demoservice.model.Elevator;
import com.example.demoservice.model.ElevatorStatus;
import com.example.demoservice.model.User;
import com.example.demoservice.repository.ElevatorRepository;
import com.example.demoservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/api/users")
@Transactional
public class HomeController {
    private final UserRepository userRepository;

    private final ElevatorRepository elevatorRepository;

    public HomeController(UserRepository userRepository, ElevatorRepository elevatorRepository) {
        this.userRepository = userRepository;
        this.elevatorRepository = elevatorRepository;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        user = userRepository.save(user);
        return ResponseEntity.created(URI.create("/v1/api/users/"+user.getId())).body(user);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> findByUserId(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found user id: "+id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @Valid @RequestBody User user) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(userRepository.save(user)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found user id: "+id));
    }

    @GetMapping(value = "{id}/buildings/{buildingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Building> findBuildingByUserAndBuilding(@PathVariable Integer id, @PathVariable Integer buildingId) {
        Optional<User> user = getUser(id);
        return user.get().getBuildings().stream()
                .filter(b -> b.getId().equals(buildingId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found building id: "+buildingId));
    }

    @GetMapping("{id}/buildings/{buildingId}/elevators/statuses")
    public ResponseEntity<List<ElevatorStatus>> getAllStatusByUserAndBuilding(@PathVariable Integer id, @PathVariable Integer buildingId) {
        Optional<User> user = getUser(id);
        return user.get().getBuildings().stream()
                .filter(b -> b.getId().equals(buildingId))
                .findFirst()
                .map(b -> b.getElevators().stream())
                .map(e -> ResponseEntity.ok(e.map(Elevator::getStatus).collect(Collectors.toList())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found building id: "+buildingId));
    }

    @PostMapping(value = "/{id}/buildings/{buildingId}/elevators/{elevatorId}/summon", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> summonElevator(@PathVariable Integer id, @PathVariable Integer buildingId,
                                                     @PathVariable Integer elevatorId,
                                                     @Valid @RequestBody SummonRequestDto summonRequestDto) {
        Elevator elevator = getElevator(id, buildingId, elevatorId);
        elevator.setStatus(summonRequestDto.getStatus());
        elevatorRepository.save(elevator);
        return ResponseEntity.ok(Collections.singletonMap("msg", "Updated elevator status"));
    }

    @PostMapping(value = "/{id}/buildings/{buildingId}/elevators/{elevatorId}/selectFloor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> floorElevator(@PathVariable Integer id, @PathVariable Integer buildingId,
                                                @PathVariable Integer elevatorId,
                                                @Valid @RequestBody SelectFloorRequestDto selectFloorRequestDto) {
        Elevator elevator = getElevator(id, buildingId, elevatorId);
        Integer floor = selectFloorRequestDto.getFloor();
        if (floor > elevator.getFloors().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected floor does not exist in list of floors");
        }
        elevator.setCurrentFloor(floor);
        elevatorRepository.save(elevator);
        return ResponseEntity.ok(Collections.singletonMap("msg", "Updated current floor"));
    }

    private Elevator getElevator(Integer id, Integer buildingId, Integer elevatorId) {
        User user = getUser(id).get();
        Building building = user.getBuildings()
                .stream().filter(b -> b.getId().equals(buildingId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "User does not belong for this building"));
        if (building.getElevators().stream().noneMatch(e -> e.getId().equals(elevatorId))) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Elevator does not belong for this building");
        }
        return elevatorRepository.findById(elevatorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found elevator id"));
    }

    private Optional<User> getUser(Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found user id: "+ id);
        }
        return user;
    }
}
