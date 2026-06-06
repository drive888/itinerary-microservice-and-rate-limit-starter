package com.fanone.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanone.user.entity.User;
import com.fanone.user.mapper.UserMapper;
import com.fanone.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final StringRedisTemplate redisTemplate;
    // 1. 注入密码编码器
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Value("${jwt.secret:fanone_itinerary_secret_key}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}")
    private Long jwtExpiration;

    public User register(String username, String password, String email) {
        // 检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 实际项目中应加密
        user.setEmail(email);
        user.setNickname(username);
        user.setStatus(1);
        this.save(user);

        return user;
    }

    public Map<String, Object> login(String username, String password) {
        // 3. 先根据用户名查出用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 使用 matches 方法比对明文和密文
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), jwtSecret, jwtExpiration);

        // 存储到Redis
        redisTemplate.opsForValue().set("token:" + user.getId(), token, jwtExpiration, TimeUnit.SECONDS);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());

        return result;
    }

    public User getUserById(Long userId) {
        return this.getById(userId);
    }
}
