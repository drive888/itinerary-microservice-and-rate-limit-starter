package com.fanone.user.controller;

import com.fanone.user.entity.User;
import com.fanone.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword(), request.getEmail());
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        Map<String, Object> data = userService.login(request.getUsername(), request.getPassword());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            user.setPassword(null); // 隐藏密码
        }
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    // --- 内部类 ---
    @lombok.Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }

    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class Result<T> {
        private int code;
        private String message;
        private T data;

        public static <T> Result<T> success(T data) {
            return new Result<>(200, "success", data);
        }

        public static <T> Result<T> error(String message) {
            return new Result<>(500, message, null);
        }
    }
}
