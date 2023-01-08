package com.example.demoservice;

import com.example.demoservice.model.Building;
import com.example.demoservice.model.Elevator;
import com.example.demoservice.model.ElevatorStatus;
import com.example.demoservice.model.User;
import com.example.demoservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
class DemoServiceApplicationTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() throws Exception {
        User user = new User();
        user.setName("test");
        Set<Building> buildings = new HashSet<>();
        Building building = new Building();
        building.setName("Building 1");
        building.setLocation("Dublin");
        Set<Elevator> elevators = new HashSet<>();
        Elevator elevator = new Elevator();
        elevator.setName("Elevator 1");
        elevator.setStatus(ElevatorStatus.STOPPED);
        elevator.setFloors(Arrays.asList(1, 2, 3, 4, 5));
        elevators.add(elevator);
        building.setElevators(elevators);
        buildings.add(building);
        user.setBuildings(buildings);

        String response = mockMvc.perform(post("/v1/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.buildings[*].name", hasSize(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isNotEmpty();

        User userResponse = objectMapper.readValue(response, User.class);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getBuildings()).isNotEmpty();
        Building next = userResponse.getBuildings().iterator().next();
        assertThat(next).isNotNull();
        assertThat(next.getElevators()).isNotEmpty();

        mockMvc.perform(get("/v1/api/users/"+userResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userResponse.getName()));

        mockMvc.perform(get(String.format("/v1/api/users/%s/buildings/%s", userResponse.getId(), next.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(next.getId()))
                .andExpect(jsonPath("$.name").value(next.getName()));

        mockMvc.perform(get(String.format("/v1/api/users/%s/buildings/%s", userResponse.getId(), next.getId() + 1)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(String.format("/v1/api/users/%s/buildings/%s/elevators/statuses", userResponse.getId(), next.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("STOPPED")));

        userResponse.setName("Another name");
        Building building2 = new Building();
        building2.setName("Building 2");
        building2.setLocation("Dublin");
        elevators = new HashSet<>();
        Elevator elevator2 = new Elevator();
        elevator2.setName("Elevator 2");
        elevator2.setStatus(ElevatorStatus.UP);
        elevator2.setFloors(Arrays.asList(1, 2, 3));
        elevators.add(elevator);
        elevators.add(elevator2);
        building2.setElevators(elevators);
        buildings = new HashSet<>();
        buildings.add(building2);

        userResponse.setBuildings(buildings);
        response = mockMvc.perform(put("/v1/api/users/"+userResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Another name"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isNotEmpty();

        userResponse = objectMapper.readValue(response, User.class);

        assertThat(userResponse).isNotNull();

        next = userResponse.getBuildings().iterator().next();
        assertThat(next).isNotNull();
        elevators = next.getElevators();
        assertThat(elevators).isNotEmpty();

        mockMvc.perform(get(String.format("/v1/api/users/%s/buildings/%s/elevators/statuses", userResponse.getId(), next.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("STOPPED", "UP")));

        Elevator elevator1 = elevators.iterator().next();
        assertThat(elevator1).isNotNull();

        mockMvc.perform(post(String.format("/v1/api/users/%s/buildings/%s/elevators/%s/summon", userResponse.getId(), next.getId(), elevator1.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("Updated elevator status"));

        mockMvc.perform(post(String.format("/v1/api/users/%s/buildings/%s/elevators/%s/summon", userResponse.getId(), next.getId(), elevator1.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Something"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(String.format("/v1/api/users/%s/buildings/%s/elevators/%s/selectFloor", userResponse.getId(), next.getId(), elevator1.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"floor\":\"3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("Updated current floor"));

        mockMvc.perform(post(String.format("/v1/api/users/%s/buildings/%s/elevators/%s/selectFloor", userResponse.getId(), next.getId(), elevator1.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"floor\":\"6\"}"))
                .andExpect(status().isBadRequest());
    }

}
