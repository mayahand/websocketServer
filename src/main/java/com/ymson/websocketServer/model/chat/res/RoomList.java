package com.ymson.websocketServer.model.chat.res;

import com.ymson.websocketServer.enumration.MessageType;
import com.ymson.websocketServer.model.ChatRoom;
import com.ymson.websocketServer.model.chat.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomList extends Message<List<ChatRoom>> {

    private ChatRoom targetRoom;

    public RoomList(MessageType messageType, ChatRoom targetRoom, List<ChatRoom> data) {
        super(messageType, data);
        this.targetRoom = targetRoom;
    }
}
