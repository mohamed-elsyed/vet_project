package com.vetclinic.controller;

import com.vetclinic.entity.User;
import com.vetclinic.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // تسجيل مستخدم جديد
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // الحصول على كل المستخدمين
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // الحصول على مستخدم عن طريق الاسم
    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
}
}