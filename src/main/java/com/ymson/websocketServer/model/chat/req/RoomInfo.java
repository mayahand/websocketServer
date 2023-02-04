package com.ymson.websocketServer.model.chat.req;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.chat.Message;
import lombok.Data;

@Data
public class RoomInfo extends Message<String> {
    private String roomId;
    private String roomName;
    private String token;

    public RoomInfo(String roomName, String token) {
        super(MessageType.ROOM_CREATE, roomName);
        this.roomName = roomName;
        this.token = token;
    }
}
