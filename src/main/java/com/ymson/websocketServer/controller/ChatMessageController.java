package com.ymson.websocketServer.controller;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.ChatRoom;
import com.ymson.websocketServer.model.chat.req.MessageInfo;
import com.ymson.websocketServer.model.chat.req.RoomInfo;
import com.ymson.websocketServer.model.chat.res.RoomList;
import com.ymson.websocketServer.model.chat.res.TextMessage;
import com.ymson.websocketServer.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final SimpMessageSendingOperations messageSendingOperations;

    private final ChatRoomRepository chatRoomRepository;

    @MessageMapping("/chat/message")
    public void sendMessage(MessageInfo messageInfo) {
        ChatRoom chatRoom = chatRoomRepository.findRoomById(messageInfo.getRoomId());
        if(chatRoom == null) {
            return;
        }

        if(chatRoom.getMemberIds().stream().noneMatch(id -> messageInfo.getUserId().equals(id))) {
            return;
        }

        chatRoom.getMemberIds().stream().forEach(userId -> {
            messageSendingOperations.convertAndSend("/sub/chat/message/".concat(userId), new TextMessage(chatRoom, messageInfo.getUserId(), messageInfo.getMessage()));
        });
    }
}
