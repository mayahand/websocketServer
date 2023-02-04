package com.ymson.websocketServer.repository;

import com.ymson.websocketServer.utils.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

    private final JwtTokenProvider provider;

    private Map<String, String> map;

    @PostConstruct
    private void init() {
        map = new LinkedHashMap<>();
    }

    public String findUserIdByToken(String token) {
        try{
            String userId = map.get(token);
            if(userId == null) {
                throw new RuntimeException();
            }
            return provider.getUserIdFromToken(token);
        }catch (Exception e) {
            map.remove(token);
            throw e;
        }
    }

    public List<String> findTokensByUserId(String userId) {
        return map.keySet().stream().map(token -> {
                    try {
                        String uId = provider.getUserIdFromToken(token);
                        if(!uId.equals(userId)) return null;
                        return token;
                    } catch (Exception e) {
                        map.remove(token);
                    }
                    return null;
                })
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String createToken(String userId) {
        String token = provider.generateToken(userId);
        map.put(token, userId);
        return token;
    }
}