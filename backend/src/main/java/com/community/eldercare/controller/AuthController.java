package com.community.eldercare.controller;

import com.community.eldercare.common.ApiException;
import com.community.eldercare.common.R;
import com.community.eldercare.domain.User;
import com.community.eldercare.dto.LoginRequest;
import com.community.eldercare.repo.UserRepository;
import com.community.eldercare.security.TokenService;
import com.community.eldercare.security.UserContext;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepo, TokenService tokenService) {
        this.userRepo = userRepo;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        User u = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new ApiException(401, "用户名或密码错误"));
        if (!u.isEnabled() || !encoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new ApiException(401, "用户名或密码错误");
        }
        String token = tokenService.issue(u);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("token", token);
        m.put("user", userView(u));
        return R.ok(m);
    }

    @PostMapping("/logout")
    public R<Void> logout(jakarta.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            tokenService.revoke(token.substring(7));
        }
        return R.ok();
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(userView(UserContext.get()));
    }

    private Map<String, Object> userView(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("name", u.getName());
        m.put("role", u.getRole());
        m.put("phone", u.getPhone());
        m.put("community", u.getCommunity());
        return m;
    }
}
