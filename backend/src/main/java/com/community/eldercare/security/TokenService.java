package com.community.eldercare.security;

import com.community.eldercare.domain.User;
import com.community.eldercare.repo.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * 基于 Redis 的登录令牌：token -> username，有效期 8 小时。
 * 值班社工/网格员/医生等多端共用。
 */
@Component
public class TokenService {

    private static final String KEY_PREFIX = "eldercare:token:";
    private static final Duration TTL = Duration.ofHours(8);

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;

    public TokenService(StringRedisTemplate redis, UserRepository userRepository) {
        this.redis = redis;
        this.userRepository = userRepository;
    }

    public String issue(User user) {
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(KEY_PREFIX + token, user.getUsername(), TTL);
        return token;
    }

    public Optional<User> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String username = redis.opsForValue().get(KEY_PREFIX + token);
        if (username == null) {
            return Optional.empty();
        }
        // 滑动续期
        redis.expire(KEY_PREFIX + token, TTL);
        return userRepository.findByUsername(username).filter(User::isEnabled);
    }

    public void revoke(String token) {
        if (token != null) {
            redis.delete(KEY_PREFIX + token);
        }
    }
}
