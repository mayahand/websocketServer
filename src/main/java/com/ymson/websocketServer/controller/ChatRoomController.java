package com.ymson.websocketServer.controller;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.ChatRoom;
import com.ymson.websocketServer.model.chat.req.RoomInfo;
import com.ymson.websocketServer.model.chat.res.RoomList;
import com.ymson.websocketServer.repository.ChatRoomRepository;
import com.ymson.websocketServer.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class ChatRoomController {
    private final SimpMessageSendingOperations messageSendingOperations;

    private final ChatRoomRepository chatRoomRepository;

    private final JwtTokenProvider jwtTokenProvider;

    @MessageMapping("/chat/rooms")
    public void getRooms(@Header("token") String token) {
        jwtTokenProvider.getUserIdFromToken(token);
        messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_LIST, null, chatRoomRepository.findAllRoom()));
    }

    @MessageMapping("/chat/rooms/new")
    public void newRoom(@Header("token") String token, RoomInfo roomInfo) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        ChatRoom chatRoom = chatRoomRepository.createChatRoom(roomInfo.getRoomName(), userId);
        messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_CREATE, chatRoom, chatRoomRepository.findAllRoom()));
    }

    @MessageMapping("/chat/rooms/join")
    public void joinRooms(@Header("token") String token, RoomInfo roomInfo) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        ChatRoom chatRoom = chatRoomRepository.findRoomById(roomInfo.getRoomId());

        if (chatRoom == null) {
            return;
        }

        if(chatRoom.getMemberIds().stream().anyMatch(mId -> mId.equals(userId))) {
            return;
        }

        chatRoom.addMembers(userId);
        messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_JOIN, chatRoom, chatRoomRepository.findAllRoom()));
    }

    @MessageMapping("/chat/rooms/leave")
    public void leaveRooms(@Header("token") String token, RoomInfo roomInfo) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        ChatRoom chatRoom = chatRoomRepository.findRoomById(roomInfo.getRoomId());

        if (chatRoom == null) {
            return;
        }

        if(chatRoom.getMemberIds().stream().noneMatch(mId -> mId.equals(userId))) {
            return;
        }

        chatRoom.leaveMembers(userId);
        if(chatRoom.getMemberIds().isEmpty()) {
            chatRoomRepository.deleteChatRoom(chatRoom.getId());
        }
        messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_LEAVE, chatRoom, chatRoomRepository.findAllRoom()));
    }
}
