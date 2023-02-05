package com.ymson.websocketServer.controller;

import com.ymson.websocketServer.model.auth.Token;
import com.ymson.websocketServer.model.user.User;
import com.ymson.websocketServer.repository.UserRepository;
import com.ymson.websocketServer.utils.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider provider;

    @PostMapping("/token")
    @ResponseBody
    public Token getToken(HttpServletResponse response,
                          @RequestParam("userId") String userId,
                          @RequestParam("password") String password) throws IOException {
        User user = userRepository.getUserById(userId);
        if(!user.getPassword().equals(password)) {
            response.addCookie(new Cookie("token", ""));
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return null;
        }

        Token token = new Token(provider.generateToken(userId));

        response.addCookie(new Cookie("token", token.getValue()));
        return token;
    }
}
