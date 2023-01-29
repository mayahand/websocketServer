package com.ymson.websocketServer.model.chat;

import com.ymson.websocketServer.enumration.MessageType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Message<T> {
    private MessageType messageType;
    private T data;

    public Message(MessageType messageType, T data) {
        this.messageType = messageType;
        this.data = data;
    }
}
