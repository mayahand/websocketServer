package com.ymson.websocketServer.model.chat.req;

import lombok.Data;

@Data
public class MessageInfo {
    private String roomId;
    private String userId;
    private String message;
}
