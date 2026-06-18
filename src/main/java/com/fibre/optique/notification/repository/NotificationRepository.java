package com.fibre.optique.notification.repository;

import com.fibre.optique.notification.entity.Notification;
import com.fibre.optique.notification.entity.NotificationChannel;
import com.fibre.optique.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataire(String destinataire);

    List<Notification> findByStatut(NotificationStatus statut);

    /** Used to retry failed notifications. */
    List<Notification> findByStatutAndTypeCanal(NotificationStatus statut, NotificationChannel canal);

    Page<Notification> findByDestinataireOrderByCreatedAtDesc(String destinataire, Pageable pageable);
}
