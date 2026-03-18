package com.ahsmart.campusmarket.service.chat;

import com.ahsmart.campusmarket.model.Chat;
import com.ahsmart.campusmarket.model.Message;
import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.payloadDTOs.chat.MessageDTO;
import com.ahsmart.campusmarket.repositories.ChatRepository;
import com.ahsmart.campusmarket.repositories.MessageRepository;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final OrderItemRepository orderItemRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public Chat createChatForOrderItem(OrderItem orderItem) {
        if (orderItem == null || orderItem.getOrderItemId() == null) {
            throw new IllegalArgumentException("Order item is required.");
        }

        return chatRepository.findByOrderItemId(orderItem.getOrderItemId())
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setOrder(orderItem.getOrder());
                    chat.setOrderItem(orderItem);
                    return chatRepository.save(chat);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Chat getChatByOrderItem(Long orderItemId) {
        if (orderItemId == null) {
            throw new IllegalArgumentException("Order item id is required.");
        }

        return chatRepository.findByOrderItemId(orderItemId)
                .orElseThrow(() -> {
                    if (!orderItemRepository.existsById(orderItemId)) {
                        return new IllegalArgumentException("Order item not found.");
                    }
                    return new IllegalArgumentException("Chat not found for order item #" + orderItemId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Chat getChatByOrderItemForUser(Long orderItemId, Long userId) {
        Chat chat = getChatByOrderItem(orderItemId);
        validateParticipant(chat, userId);
        return chat;
    }

    @Override
    @Transactional(readOnly = true)
    public Chat getChatById(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat id is required.");
        }
        return chatRepository.findByIdWithParticipants(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public Chat getChatByIdForUser(Long chatId, Long userId) {
        Chat chat = getChatById(chatId);
        validateParticipant(chat, userId);
        return chat;
    }

    @Override
    @Transactional
    public MessageDTO sendMessage(Long chatId, Long userId, String message) {
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in to send messages.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }
        String normalizedMessage = message.trim();
        if (normalizedMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message cannot exceed " + MAX_MESSAGE_LENGTH + " characters.");
        }

        Chat chat = getAuthorizedChat(chatId, userId);

        Users sender = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Message entity = new Message();
        entity.setChat(chat);
        entity.setSender(sender);
        entity.setMessage(normalizedMessage);

        Message saved = messageRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(Long chatId, Long userId) {
        getChatByIdForUser(chatId, userId);
        return messageRepository.findByChatIdOrderBySentAtAsc(chatId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Chat getAuthorizedChat(Long chatId, Long userId) {
        return getChatByIdForUser(chatId, userId);
    }

    private void validateParticipant(Chat chat, Long userId) {
        if (userId == null) {
            throw new SecurityException("You must be logged in to access this chat.");
        }

        Long buyerId = chat.getOrderItem().getOrder().getBuyer().getUserId();
        Long sellerUserId = chat.getOrderItem().getSeller().getUser().getUserId();
        if (!buyerId.equals(userId) && !sellerUserId.equals(userId)) {
            throw new SecurityException("You are not allowed to access this chat.");
        }
    }

    private MessageDTO toDto(Message message) {
        Users sender = message.getSender();
        String senderName = ((sender.getFirstName() == null ? "" : sender.getFirstName().trim()) + " "
                + (sender.getLastName() == null ? "" : sender.getLastName().trim())).trim();
        if (senderName.isEmpty()) {
            senderName = sender.getEmail();
        }

        return new MessageDTO(
                message.getMessageId(),
                message.getChat().getChatId(),
                sender.getUserId(),
                senderName,
                message.getMessage(),
                message.getSentAt()
        );
    }
}
