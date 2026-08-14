package com.example.ms.user.service;

import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.user.dto.CaptchaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate stringRedisTemplate;

    public CaptchaResponse generateCaptcha() {
        String captchaId = UUID.randomUUID().toString().substring(0, 8);
        String captcha = String.format("%04d", RANDOM.nextInt(10000));
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaId, captcha, CAPTCHA_TTL);
        return new CaptchaResponse(captchaId, captcha);
    }

    public void verifyCaptcha(String captchaId, String captchaText) {
        String key = CAPTCHA_PREFIX + captchaId;
        String stored = stringRedisTemplate.opsForValue().get(key);
        if (stored == null || !stored.equals(captchaText)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误或已过期");
        }
        // 一次性：校验通过立即删除，防重放
        stringRedisTemplate.delete(key);
    }
}
