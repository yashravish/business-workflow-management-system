// Translated from: backend/app/api/routes/users.py
package com.example.bwms.controller;

import com.example.bwms.dto.UserSummaryDto;
import com.example.bwms.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Python: GET /users — used by the frontend assignee dropdown; any authenticated user can call
    @GetMapping
    public List<UserSummaryDto> listUsers() {
        return userRepository.findAllByOrderByNameAsc().stream()
                .map(UserSummaryDto::from)
                .toList();
    }
}
