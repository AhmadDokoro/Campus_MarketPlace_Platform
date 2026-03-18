package com.ahsmart.campusmarket.service.chat;

import com.ahsmart.campusmarket.model.Chat;
import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.payloadDTOs.chat.MessageDTO;

import java.util.List;

public interface ChatService {

    Chat createChatForOrderItem(OrderItem orderItem);

    Chat getChatByOrderItem(Long orderItemId);

    Chat getChatByOrderItemForUser(Long orderItemId, Long userId);

    Chat getChatById(Long chatId);

    Chat getChatByIdForUser(Long chatId, Long userId);

    MessageDTO sendMessage(Long chatId, Long userId, String message);

    List<MessageDTO> getMessages(Long chatId, Long userId);
}
