package com.ymson.websocketServer.model.chat.res;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.ChatRoom;
import com.ymson.websocketServer.model.chat.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextMessage extends Message<String> {
    private ChatRoom chatRoom;
    private String userId;
    private String userName;

    public TextMessage(ChatRoom chatRoom, String userId, String userName, String msg) {
        super(MessageType.MESSAGE_SEND, msg);
        this.chatRoom = chatRoom;
        this.userId = userId;
        this.userName = userName;
    }
}
