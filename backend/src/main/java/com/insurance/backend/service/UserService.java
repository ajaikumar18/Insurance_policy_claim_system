package com.insurance.backend.service;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.User;

import java.util.List;

public interface UserService {
    AuthResponse login(LoginRequest request);
    OnboardResponse onboardPolicyholder(OnboardRequest request);
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(UserCreateRequest request);
}
