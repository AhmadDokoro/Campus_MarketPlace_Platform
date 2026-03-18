package com.ahsmart.campusmarket.payloadDTOs.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatDTO {
    private Long chatId;
    private Long orderItemId;
    private Long orderId;
}
