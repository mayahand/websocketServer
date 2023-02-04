package com.ymson.websocketServer.repository;

import com.ymson.websocketServer.model.ChatRoom;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ChatRoomRepository {

    private Map<String, ChatRoom> chatRoomMap;

    @PostConstruct
    private void init() {
        chatRoomMap = new LinkedHashMap<>();
    }

    public List<ChatRoom> findAllRoom() {
        // 채팅방 생성순서 최근 순으로 반환
        List chatRooms = new ArrayList<>(chatRoomMap.values());
        Collections.reverse(chatRooms);
        return chatRooms;
    }

    public ChatRoom findRoomById(String id) {
        return chatRoomMap.get(id);
    }

    public ChatRoom createChatRoom(String name, String ownerId) {
        ChatRoom chatRoom = ChatRoom.create(name, ownerId);
        chatRoomMap.put(chatRoom.getId(), chatRoom);
        return chatRoom;
    }

    public ChatRoom deleteChatRoom(String roomId) {
        ChatRoom deleteRom = findRoomById(roomId);
        chatRoomMap.remove(roomId);
        return deleteRom;
    }
}