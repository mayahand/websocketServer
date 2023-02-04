package com.ymson.websocketServer.repository;

import com.ymson.websocketServer.model.user.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private Map<String, User> map;

    @PostConstruct
    private void init() {
        map = new LinkedHashMap<>();
        map.put("ymson", new User("ymson", "손영민1", "1q2w3e!@"));
        map.put("ymson2", new User("ymson2", "손영민2", "1q2w3e!@"));
    }

    public User getUserById(String id) {
        return map.get(id);
    }
}
