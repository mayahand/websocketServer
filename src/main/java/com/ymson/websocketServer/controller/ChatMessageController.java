package com.ymson.websocketServer.controller;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.ChatRoom;
import com.ymson.websocketServer.model.chat.req.MessageInfo;
import com.ymson.websocketServer.model.chat.req.RoomInfo;
import com.ymson.websocketServer.model.chat.res.RoomList;
import com.ymson.websocketServer.model.chat.res.TextMessage;
import com.ymson.websocketServer.repository.ChatRoomRepository;
import com.ymson.websocketServer.repository.TokenRepository;
import com.ymson.websocketServer.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final SimpMessageSendingOperations messageSendingOperations;

    private final ChatRoomRepository chatRoomRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    @MessageMapping("/chat/message")
    public void sendMessage(MessageInfo messageInfo) {
        String userId = jwtTokenProvider.getUserIdFromToken(messageInfo.getToken());
        ChatRoom chatRoom = chatRoomRepository.findRoomById(messageInfo.getRoomId());
        if(chatRoom == null) {
            return;
        }

        if(chatRoom.getMemberIds().stream().noneMatch(id -> userId.equals(id))) {
            return;
        }

        chatRoom.getMemberIds().stream().forEach(memberId -> {
            tokenRepository.findTokensByUserId(memberId).stream()
                    .forEach(token -> messageSendingOperations.convertAndSend(
                            "/sub/chat/message/".concat(token),
                            new TextMessage(chatRoom, userId, messageInfo.getMessage())
                    ));
        });
    }
}
