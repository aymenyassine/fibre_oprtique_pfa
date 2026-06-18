package com.fibre.optique.support.repository;

import com.fibre.optique.support.entity.Ticket;
import com.fibre.optique.support.entity.TicketPriority;
import com.fibre.optique.support.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByClientId(Long clientId);

    List<Ticket> findByAgentId(Long agentId);

    List<Ticket> findByStatut(TicketStatus statut);

    /** Used by SLA scheduler — open tickets past their deadline. */
    @Query("""
            SELECT t FROM Ticket t
            WHERE t.statut NOT IN :closedStatuts
              AND t.dateLimiteSla < :now
            """)
    List<Ticket> findSlaBreached(
            @Param("now") Instant now,
            @Param("closedStatuts") List<TicketStatus> closedStatuts
    );

    /** Paginated search for staff. */
    @Query("""
            SELECT t FROM Ticket t
            WHERE (:clientId IS NULL OR t.client.id = :clientId)
              AND (:agentId  IS NULL OR t.agent.id  = :agentId)
              AND (:statut   IS NULL OR t.statut     = :statut)
              AND (:priorite IS NULL OR t.priorite   = :priorite)
              AND (:keyword  IS NULL
                   OR LOWER(t.titre)       LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY t.dateCreation DESC
            """)
    Page<Ticket> search(
            @Param("clientId")  Long clientId,
            @Param("agentId")   Long agentId,
            @Param("statut")    TicketStatus statut,
            @Param("priorite")  TicketPriority priorite,
            @Param("keyword")   String keyword,
            Pageable pageable
    );
}
