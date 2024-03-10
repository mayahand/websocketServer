import {
  Component,
  OnInit,
  ElementRef,
  ViewChild,
  Inject,
  AfterViewChecked
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Stomp, CompatClient } from '@stomp/stompjs';
import * as SockJS from "sockjs-client";
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {AuthService} from "../auth/auth.service";
import {MatDialog, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {environment} from "../../environments/environment";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit, AfterViewChecked{
  @ViewChild('messageScroller') messageScroller: ElementRef<HTMLElement>;
  messageScrollerObserver: ScrollerAbleHTMLElementObserver;

  stompClient: CompatClient;
  userId: string;
  token: string;
  roomName: string;
  message: string;
  roomList: ChatRoom[] = [];
  currentChatRoom: ChatRoom | undefined;
  constructor(private route: ActivatedRoute,
              private authService: AuthService,
              public dialog: MatDialog) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.userId = params['userId'];

      this.authService.getToken(this.userId, params['pw']).subscribe({
        next: (token: any) => {
          this.token = token.value;
          this.connect(token.value);
        },
        error: () => this.openDialog("로그인 실패")
      });


    });
  }

  ngAfterViewChecked() {
    if(!!this.messageScroller && !this.messageScrollerObserver) {
      this.messageScrollerObserver = ScrollerAbleHTMLElementObserver.create(this.messageScroller);
    }
  }

  connect(token: string) {

    const _this = this;

    if(!token) {
      return;
    }

    if(_this.stompClient != null) {
      return;
    }

    const socket = new SockJS(environment.chatApiDomain+'/ws-stomp');
    _this.stompClient = Stomp.over(socket);
    _this.stompClient.connect({token:token}, () => {
      _this.stompClient.subscribe('/sub/chat/message/'+this.userId, function (data) {
        let message = JSON.parse(data.body);
        _this.roomList.filter((room: ChatRoom) => room.id === message.chatRoom.id).forEach((room: ChatRoom) => {
          room.messages.push(message);
          _this.markRead();
        });
      }, {
        token: token
      });

      _this.stompClient.subscribe('/sub/chat/rooms', function (data) {
        let message = JSON.parse(data.body);

        switch (message.messageType) {
          case 'ROOM_LEAVE': {
            if(!message.targetRoom || !_this.currentChatRoom || message.targetRoom.id !== _this.currentChatRoom.id) {
              break;
            }

            if(message.targetRoom.memberIds.some((mId: string) => mId === _this.userId)) {
              break;
            }

            _this.currentChatRoom = undefined;
          }
        }

        if(message.data == null) {
          return;
        }

        // 삭제된 방 동기화
        _this.roomList = _this.roomList.filter(room => message.data.some((newRoom : {
          id: string;
          name: string;
          ownerId: string;
          memberIds: string[];
        }) => {
          if(room.id === newRoom.id) {
            room.name = newRoom.name;
            room.ownerId = newRoom.ownerId;
            room.memberIds = newRoom.memberIds;
            room.checkMember(_this.userId);
          }
          return room.id === newRoom.id
        }));

        // 추가된 방 동기화
        message.data.filter((newRoom : {
          id: string;
          name: string;
          ownerId: string;
          memberIds: string[];
        }) => !_this.roomList.some(room => room.id === newRoom.id))
          .forEach((room: { id: string; name: string; ownerId: string; memberIds: string[]; }) => {
            const chatRoom: ChatRoom = new ChatRoom();
            chatRoom.id = room.id;
            chatRoom.name = room.name;
            chatRoom.ownerId = room.ownerId;
            chatRoom.memberIds = room.memberIds;
            chatRoom.checkMember(_this.userId);
            _this.roomList.push(chatRoom);
          });
      }, {
        token: token
      });

      _this.stompClient.send('/pub/chat/rooms', {token:this.token});
    });
  }

  joinRoom(roomId: string) {
    this.stompClient.send('/pub/chat/rooms/join', {token:this.token}, JSON.stringify({roomId:roomId}));
  }

  leaveRoom(roomId: string) {
    this.stompClient.send('/pub/chat/rooms/leave', {token:this.token}, JSON.stringify({roomId:roomId}));
  }

  createRoom(roomName: string) {
    this.stompClient.send("/pub/chat/rooms/new", {token:this.token}, JSON.stringify({roomName:roomName}));
  }

  sendMessage(roomId: string | undefined, message: string) {
    if(roomId == undefined) {
      this.message = '';
      return;
    }

    if(!this.message) {
      return;
    }
    this.stompClient.send('/pub/chat/message', {token:this.token}, JSON.stringify({roomId:roomId, message:message}));
    this.message = '';
  }

  markRead() {
    if(!!this.currentChatRoom) {
      this.currentChatRoom.messages.filter(msg => !msg.isRead).forEach(msg => msg.isRead = true);
    }
  }

  drop(event: CdkDragDrop<string[]>, list: any[]) {
    if (!list) {
      return;
    }

    moveItemInArray(list, event.previousIndex, event.currentIndex);
  }

  openDialog(msg: string) {
    this.dialog.open(ChatDialog, {
      data:msg
    });
  }
}

class ChatRoom {
  id: string = '';
  name: string = '';
  ownerId: string = '';
  memberIds: string[] = [];
  isMember: boolean = false;
  messages: Message[] = [];
  checkMember(userId: string) {
    this.isMember = this.memberIds.some(memberId => memberId === userId);
  }

  unreadCount() {
    return this.messages.filter(m => !m.isRead).length;
  }
}

class Message {
  userId: string;
  userName: string;
  chatRoom: ChatRoom;
  data: any;
  isRead: boolean = false;
}

// scrollToBottom
class ScrollerAbleHTMLElementObserver extends MutationObserver {
  element: ScrollerAbleHTMLElement;
  constructor(sEle: ScrollerAbleHTMLElement) {
    super(mutationsList => {
      for (let mutation of mutationsList) {
        if (mutation.type === "childList" && sEle.isBottom) {
          sEle.scrollToBottom();
        }
      }
    });
    sEle.checkScrollBottom();
    sEle.element.addEventListener('scroll', () => sEle.checkScrollBottom());
    this.observe(sEle.element, { childList: true});
    this.element = sEle;
  }

  public static create(element: ElementRef<HTMLElement>): ScrollerAbleHTMLElementObserver {
    return new ScrollerAbleHTMLElementObserver(new ScrollerAbleHTMLElement(element.nativeElement));
  }
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
    if (this.element.scrollTop + this.element.offsetHeight >= this.element.scrollHeight) {
      this.isBottom = true;
    }
  }

  scrollToBottom() {
    this.element.scrollTo(0, this.element.scrollHeight);
  }
}

@Component({
  selector: 'chat-dialog',
  templateUrl: './chat.dialog.html',
  styleUrls: ['./chat.dialog.scss']
})
export class ChatDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: string) {}
}
