package com.isyraf.kuantanfunmap.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public List<UserResponse> getAllUser() {
        var users = repository.findAll();

        return users
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFirstname(),
                        user.getLastname(),
                        user.getEmail(),
                        user.getRole()
                )).toList();
    }
}
