package com.ymson.websocketServer.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
public class ChatRoom {
    public static ChatRoom create(String roomName, String ownerId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(UUID.randomUUID().toString());
        chatRoom.setName(roomName);
        chatRoom.setOwnerId(ownerId);
        chatRoom.addMembers(ownerId);
        return chatRoom;
    }

    private String id;
    private String name;
    private String ownerId;
    private List<String> memberIds;

    public void addMembers(String... memberIds) {
        if(this.memberIds == null){
            this.memberIds = new ArrayList<>();
        }

        Arrays.stream(memberIds).forEach(memberId -> {
            if(this.memberIds.stream().anyMatch(mId -> memberId.equals(mId))){
                return;
            }

            this.memberIds.add(memberId);
        });
    }

    public void leaveMembers(String... memberIds) {
        if(this.memberIds == null){
            this.memberIds = new ArrayList<>();
            return;
        }

        Arrays.stream(memberIds).forEach(memberId -> {
            this.memberIds.remove(memberId);
        });
    }

    public void clearMembers() {
        if(this.memberIds == null){
            this.memberIds = new ArrayList<>();
            return;
        }

        this.memberIds.clear();
    }
}
