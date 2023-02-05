package com.ymson.websocketServer.interceptor;

import com.ymson.websocketServer.utils.JwtTokenProvider;
import io.jsonwebtoken.MalformedJwtException;
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

    private final JwtTokenProvider provider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        switch (accessor.getCommand()) {
            case CONNECT:
            case SUBSCRIBE:
                String userId = provider.getUserIdFromToken(accessor.getFirstNativeHeader("token"));
                if(Strings.isEmpty(userId)){
                    throw new RuntimeException();
                }

                if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
                    String dest = accessor.getDestination();
                    String[] destChunk = dest.split("/");
                    if (dest.startsWith("/sub/chat/message/") && destChunk.length > 4) {
                        if(!userId.equals(destChunk[4])) {
                            throw new MalformedJwtException("invalid token");
                        }
                    }
                }
                break;
        }
        return message;
    }
}
