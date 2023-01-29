package com.ymson.websocketServer.controller;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.ChatRoom;
import com.ymson.websocketServer.model.chat.req.RoomInfo;
import com.ymson.websocketServer.model.chat.res.RoomList;
import com.ymson.websocketServer.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class ChatRoomController {
    private final SimpMessageSendingOperations messageSendingOperations;

    private final ChatRoomRepository chatRoomRepository;

    @MessageMapping("/chat/rooms")
    public void getRooms() {
        messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_LIST, null, chatRoomRepository.findAllRoom()));
    }

    @MessageMapping("/chat/rooms/new")
    public void newRoom(RoomInfo roomInfo) {
        ChatRoom chatRoom = chatRoomRepository.createChatRoom(roomInfo.getRoomName(), roomInfo.getUserId());
        messageSendingOperations.convertAndSend("/sub/chat/rooms/".concat(roomInfo.getUserId()), new RoomList(MessageType.ROOM_JOIN, chatRoom, chatRoomRepository.findAllRoom()));
        messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_CREATE, chatRoom, chatRoomRepository.findAllRoom()));
    }

    @MessageMapping("/chat/rooms/join")
    public void joinRooms(RoomInfo roomInfo) {
        ChatRoom chatRoom = chatRoomRepository.findRoomById(roomInfo.getRoomId());
        messageSendingOperations.convertAndSend("/sub/chat/rooms/".concat(roomInfo.getUserId()), new RoomList(MessageType.ROOM_JOIN, chatRoom, chatRoomRepository.findAllRoom()));
        if (chatRoom != null) {
            chatRoom.addMembers(roomInfo.getUserId());
            messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_JOIN, chatRoom, chatRoomRepository.findAllRoom()));
        }
    }

    @MessageMapping("/chat/rooms/leave")
    public void leaveRooms(RoomInfo roomInfo) {
        ChatRoom chatRoom = chatRoomRepository.findRoomById(roomInfo.getRoomId());
        messageSendingOperations.convertAndSend("/sub/chat/rooms/".concat(roomInfo.getUserId()), new RoomList(MessageType.ROOM_LEAVE, chatRoom, chatRoomRepository.findAllRoom()));
        if (chatRoom != null) {
            chatRoom.leaveMembers(roomInfo.getUserId());
            messageSendingOperations.convertAndSend("/sub/chat/rooms", new RoomList(MessageType.ROOM_LEAVE, chatRoom, chatRoomRepository.findAllRoom()));
        }
    }
}
