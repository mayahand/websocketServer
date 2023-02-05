package com.ymson.websocketServer.model.chat.req;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.chat.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RoomInfo extends Message<String> {
    private String roomId;
    private String roomName;

    public RoomInfo(String roomName) {
        super(MessageType.ROOM_CREATE, roomName);
        this.roomName = roomName;
    }
}
