package org.puregxl.careercoachdemo.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.careercoachdemo.mapper.UserMapper;
import org.puregxl.careercoachdemo.model.User;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {
    private final UserMapper userMapper;

    @PostMapping("/users")
    public User createUser(@RequestParam String name, @RequestParam String email) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setName(name);
            user.setEmail(email);
            userMapper.insert(user);
        }
        return user;
    }

    @GetMapping("/users")
    public User getUser(@RequestParam String email) {
        return userMapper.findByEmail(email);
    }
}
