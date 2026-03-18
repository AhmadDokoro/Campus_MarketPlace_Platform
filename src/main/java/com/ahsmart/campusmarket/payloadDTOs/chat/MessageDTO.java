package com.ahsmart.campusmarket.payloadDTOs.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageDTO {
    private Long messageId;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String message;
    private LocalDateTime sentAt;
}
