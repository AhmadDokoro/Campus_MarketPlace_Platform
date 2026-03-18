package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Chat;
import com.ahsmart.campusmarket.payloadDTOs.chat.ChatDTO;
import com.ahsmart.campusmarket.payloadDTOs.chat.MessageDTO;
import com.ahsmart.campusmarket.service.chat.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/order-item/{orderItemId}/page")
    public String chatPage(@PathVariable Long orderItemId, HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

        try {
            Chat chat = chatService.getChatByOrderItemForUser(orderItemId, userId);
            model.addAttribute("chat", chat);
            model.addAttribute("messages", chatService.getMessages(chat.getChatId(), userId));
            model.addAttribute("currentUserId", userId);
            model.addAttribute("orderItem", chat.getOrderItem());
            model.addAttribute("backUrl", resolveBackUrl(session));
            return "chat/chat";
        } catch (SecurityException ex) {
            return "redirect:/index";
        } catch (IllegalArgumentException ex) {
            return "redirect:/index";
        }
    }

    @ResponseBody
    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<?> getChatByOrderItem(@PathVariable Long orderItemId, HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "You must be logged in."));
        }

        try {
            Chat chat = chatService.getChatByOrderItemForUser(orderItemId, userId);
            return ResponseEntity.ok(new ChatDTO(chat.getChatId(), chat.getOrderItem().getOrderItemId(), chat.getOrderItem().getOrder().getOrderId()));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @ResponseBody
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestParam("chatId") Long chatId,
                                         @RequestParam("message") String message,
                                         HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "You must be logged in."));
        }

        try {
            MessageDTO saved = chatService.sendMessage(chatId, userId, message);
            return ResponseEntity.ok(saved);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @ResponseBody
    @GetMapping("/{chatId}")
    public ResponseEntity<?> getMessages(@PathVariable Long chatId, HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "You must be logged in."));
        }

        try {
            List<MessageDTO> messages = chatService.getMessages(chatId, userId);
            return ResponseEntity.ok(messages);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private Long resolveUserId(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return null;
        }
        return (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));
    }

    private String resolveBackUrl(HttpSession session) {
        Object role = session.getAttribute("role");
        if (role != null && "SELLER".equals(String.valueOf(role))) {
            return "/seller/orders";
        }
        return "/user/profile";
    }
}
