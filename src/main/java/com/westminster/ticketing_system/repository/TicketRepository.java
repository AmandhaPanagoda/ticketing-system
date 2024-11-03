package com.westminster.ticketing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.westminster.ticketing_system.entity.Ticket;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByUserId(int userId);

    List<Ticket> findByPurchaserId(int purchaserId);

    List<Ticket> findByPurchasedFalseAndDeletedIndFalse();
}
