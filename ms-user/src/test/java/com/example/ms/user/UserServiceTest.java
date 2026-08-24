package com.example.ms.user;

import com.example.ms.user.dto.CaptchaResponse;
import com.example.ms.user.dto.LoginRequest;
import com.example.ms.user.dto.LoginResponse;
import com.example.ms.user.service.CaptchaService;
import com.example.ms.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @Test
    void login() {
        CaptchaResponse captcha = captchaService.generateCaptcha();

        LoginRequest request = new LoginRequest();
        request.setPhone("13800000000");
        request.setPassword("123456");
        request.setCaptchaId(captcha.getCaptchaId());
        request.setCaptcha(captcha.getCaptcha());

        LoginResponse response = userService.login(request);
        System.out.println("token = " + response.getToken());
    }
}
