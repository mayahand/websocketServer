import { Component, OnInit, HostListener } from '@angular/core';
import { Stomp, CompatClient } from '@stomp/stompjs';
import * as SockJS from "sockjs-client";
import * as $ from "jquery";
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit{
  stompClient: CompatClient;
  userId: string;
  roomName: string;
  message: string;
  messages: Message[] = [];
  roomList: ChatRoom[] = [];
  chatRoomId: string | undefined;
  scrollableTarget: ScrollerAbleHTMLElement[] = [];
  constructor(private route: ActivatedRoute) {
  }

  @HostListener('scroll', ['$event'])
  onElementScroll($event: any) {
    const target: ScrollerAbleHTMLElement | undefined = this.scrollableTarget.find(ele => ele.element === $event.target);
    if(target === undefined) {
      return;
    }
    target.checkScrollBottom();
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.userId = params['userId'];
    });

    this.scrollableTarget.push(new ScrollerAbleHTMLElement(document.getElementById("messageScroller")!));
    this.scrollableTarget.forEach(ele => {
      ele.checkScrollBottom();
      const config = { childList: true};
      const observer = new MutationObserver(function (mutationsList) {
        for (let mutation of mutationsList) {
          if (mutation.type === "childList" && ele.isBottom) {
            ele.scrollToBottom();
          }
        }
      });
      observer.observe(ele.element, config);
    });

    this.connect(this.userId);
  }

  connect(userId: string) {
    const _this = this;

    if(!userId) {
      return;
    }

    if(_this.stompClient != null) {
      return;
    }

    const socket = new SockJS('http://localhost:9101/ws-stomp');
    _this.stompClient = Stomp.over(socket);
    _this.stompClient.connect({}, () => {
      _this.stompClient.subscribe('/sub/chat/message/'+userId, function (data) {
        let message = JSON.parse(data.body);
        _this.messages.push(message);
      });

      _this.stompClient.subscribe('/sub/chat/rooms/'+userId, function (data) {
        let message = JSON.parse(data.body);

        if(message.targetRoom == null) {
          return;
        }
      });

      _this.stompClient.subscribe('/sub/chat/rooms', function (data) {
        let message = JSON.parse(data.body);

        if(message.data == null) {
          return;
        }

        _this.roomList = message.data.map((room: { id: string; name: string; ownerId: string; memberIds: string[]; }) => {
          const chatRoom: ChatRoom = new ChatRoom();
          chatRoom.id = room.id;
          chatRoom.name = room.name;
          chatRoom.ownerId = room.ownerId;
          chatRoom.memberIds = room.memberIds;
          chatRoom.checkMember(_this.userId);
          return chatRoom;
        });
      });

      _this.stompClient.send('/pub/chat/rooms', {});
    });
  }

  joinRoom(roomId: string) {
    this.stompClient.send('/pub/chat/rooms/join', {}, JSON.stringify({roomId:roomId, userId:this.userId}));
  }

  leaveRoom(roomId: string) {
    if(this.chatRoomId === roomId) {
      this.chatRoomId = undefined;
    }
    this.stompClient.send('/pub/chat/rooms/leave', {}, JSON.stringify({roomId:roomId, userId:this.userId}));
  }

  createRoom(roomName: string) {
    this.stompClient.send("/pub/chat/rooms/new", {}, JSON.stringify({roomName:roomName, userId:this.userId}));
  }

  sendMessage(roomId: string | undefined, message: string) {
    if(roomId == undefined) {
      this.message = '';
      return;
    }

    if(this.message === '' || this.message == null || this.message == undefined) {
      return;
    }
    this.stompClient.send('/pub/chat/message', {}, JSON.stringify({roomId:roomId, userId:this.userId, message:message}));
    this.message = '';
  }
}

class ChatRoom {
  id: string = '';
  name: string = '';
  ownerId: string = '';
  memberIds: string[] = [];
  isMember: boolean = false;
  checkMember(userId: string) {
    this.isMember = this.memberIds.some(memberId => memberId === userId);
  }
}

class Message {
  userId: string;
  chatRoom: ChatRoom;
  data: any;
}

class ScrollerAbleHTMLElement {

  element: HTMLElement;
  isBottom: boolean;

  constructor(element: HTMLElement) {
    this.element = element;
  }

  checkScrollBottom() {
    this.isBottom = false;
    // @ts-ignore
    if ($(this.element).scrollTop() + $(this.element).innerHeight() >= this.element.scrollHeight) {
      this.isBottom = true;
    }
  }

  scrollToBottom() {
    this.element.scrollTo(0, this.element.scrollHeight);
  }
}
