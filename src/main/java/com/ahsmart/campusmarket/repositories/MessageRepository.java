package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m " +
            "join fetch m.sender " +
            "where m.chat.chatId = :chatId " +
            "order by m.sentAt asc")
    List<Message> findByChatIdOrderBySentAtAsc(@Param("chatId") Long chatId);
}
