package com.ymson.websocketServer.interceptor;

import com.ymson.websocketServer.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StompChannelInterceptor implements ChannelInterceptor {
    private final TokenRepository tokenRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            String userId = tokenRepository.findUserIdByToken(accessor.getFirstNativeHeader("token"));
           if(Strings.isEmpty(userId)){
               throw new RuntimeException();
           }
        }
        return message;
    }
}
