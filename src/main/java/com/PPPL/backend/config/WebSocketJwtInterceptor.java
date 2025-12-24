package com.PPPL.backend.config;


import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {



    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // Izinkan CONNECT dan SUBSCRIBE tanpa validasi JWT
        if (StompCommand.CONNECT.equals(accessor.getCommand())
            || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        // JWT VALIDATION UNTUK SEND SAJA
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing JWT token");
        }

        return message;
    }
}
