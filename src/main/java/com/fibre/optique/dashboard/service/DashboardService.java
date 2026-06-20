package com.fibre.optique.dashboard.service;

import com.fibre.optique.dashboard.dto.DashboardStatsDto;
import com.fibre.optique.network.repository.CheminFibreRepository;
import com.fibre.optique.offer.repository.OfferRepository;
import com.fibre.optique.support.entity.TicketStatus;
import com.fibre.optique.support.repository.TicketRepository;
import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final CheminFibreRepository cheminFibreRepository;

    public DashboardService(OfferRepository offerRepository,
                            UserRepository userRepository,
                            TicketRepository ticketRepository,
                            CheminFibreRepository cheminFibreRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.cheminFibreRepository = cheminFibreRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        long activeOffers = offerRepository.countByActifTrue();
        long clients = userRepository.countByRole(Role.CLIENT);
        long openTickets = ticketRepository.countByStatut(TicketStatus.OUVERT) 
                         + ticketRepository.countByStatut(TicketStatus.EN_COURS);
        long incidents = cheminFibreRepository.countByStatut("INCIDENT");

        return DashboardStatsDto.builder()
                .activeOffersCount(activeOffers)
                .clientsCount(clients)
                .openTicketsCount(openTickets)
                .networkIncidentsCount(incidents)
                .build();
    }
}
