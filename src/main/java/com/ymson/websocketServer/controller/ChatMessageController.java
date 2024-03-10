package com.ymson.websocketServer.controller;

import com.ymson.websocketServer.model.ChatRoom;
import com.ymson.websocketServer.model.chat.req.MessageInfo;
import com.ymson.websocketServer.model.chat.res.TextMessage;
import com.ymson.websocketServer.model.user.User;
import com.ymson.websocketServer.repository.ChatRoomRepository;
import com.ymson.websocketServer.repository.UserRepository;
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
    private final UserRepository userRepository;

    @MessageMapping("/chat/message")
    public void sendMessage(@Header("token") String token, MessageInfo messageInfo) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.getUserById(userId);
        ChatRoom chatRoom = chatRoomRepository.findRoomById(messageInfo.getRoomId());
        if(chatRoom == null) {
            return;
        }

        if(chatRoom.getMemberIds().stream().noneMatch(id -> userId.equals(id))) {
            return;
        }

        chatRoom.getMemberIds().stream()
                .forEach(memberId -> messageSendingOperations.convertAndSend(
                        "/sub/chat/message/".concat(memberId),
                        new TextMessage(chatRoom, user.getId(), user.getName(), messageInfo.getMessage())
                        ));
    }
}
