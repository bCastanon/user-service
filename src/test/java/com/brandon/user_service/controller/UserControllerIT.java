package com.brandon.user_service.controller;

import com.brandon.user_service.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAndGetUser_shouldWorkEndToEnd() {
        UserRequest request = new UserRequest();
        request.setName("Carlos");
        request.setEmail("carlos@test.com");

        ResponseEntity<Object> createResponse = restTemplate.postForEntity("/users", request, Object.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createUser_shouldReturn400_whenEmailInvalid() {
        UserRequest request = new UserRequest();
        request.setName("Sin Email");
        request.setEmail("no-es-un-email");

        ResponseEntity<Object> response = restTemplate.postForEntity("/users", request, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createUser_shouldReturn409_whenEmailDuplicated() {
        UserRequest request = new UserRequest();
        request.setName("Duplicado");
        request.setEmail("duplicado@test.com");

        restTemplate.postForEntity("/users", request, Object.class);
        ResponseEntity<Object> secondResponse = restTemplate.postForEntity("/users", request, Object.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getUser_shouldReturn404_whenNotExists() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/users/999999", Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
