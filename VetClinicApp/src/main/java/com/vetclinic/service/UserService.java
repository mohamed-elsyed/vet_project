package com.vetclinic.service;

import com.vetclinic.entity.User;

import com.vetclinic.dto.RegisterRequest;
import com.vetclinic.dto.UserResponseDTO;
import java.util.List;

public interface UserService {
    User registerUser(User user);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    UserResponseDTO register(RegisterRequest request);
    }
