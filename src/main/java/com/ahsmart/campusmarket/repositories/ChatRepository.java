package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("select c from Chat c " +
            "join fetch c.orderItem oi " +
            "join fetch oi.product " +
            "join fetch oi.order o " +
            "join fetch o.buyer " +
            "join fetch oi.seller s " +
            "join fetch s.user " +
            "where oi.orderItemId = :orderItemId")
    Optional<Chat> findByOrderItemId(@Param("orderItemId") Long orderItemId);

    @Query("select c from Chat c " +
            "join fetch c.orderItem oi " +
            "join fetch oi.product " +
            "join fetch oi.order o " +
            "join fetch o.buyer " +
            "join fetch oi.seller s " +
            "join fetch s.user " +
            "where c.chatId = :chatId")
    Optional<Chat> findByIdWithParticipants(@Param("chatId") Long chatId);
}
