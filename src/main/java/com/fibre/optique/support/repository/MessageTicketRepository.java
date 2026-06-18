package com.fibre.optique.support.repository;

import com.fibre.optique.support.entity.MessageTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageTicketRepository extends JpaRepository<MessageTicket, Long> {

    List<MessageTicket> findByTicketIdOrderByDateEnvoiAsc(Long ticketId);
}
