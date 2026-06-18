package com.fibre.optique.notification.service;

import com.fibre.optique.notification.dto.NotificationDto;
import com.fibre.optique.notification.entity.Notification;
import com.fibre.optique.notification.entity.NotificationChannel;
import com.fibre.optique.notification.entity.NotificationStatus;
import com.fibre.optique.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /** Gmail address used as the From header — must match spring.mail.username */
    @Value("${spring.mail.username}")
    private String fromAddress;

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public NotificationService(NotificationRepository notificationRepository,
                                JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    // =========================================================================
    // PUBLIC API — called by listener and controller
    // =========================================================================

    /**
     * Persists and sends an email notification.
     * The notification record is saved as EN_ATTENTE first, then updated
     * to ENVOYE or ECHEC after the send attempt.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    plain-text body
     * @return the saved Notification entity
     */
    @Transactional
    public Notification sendEmail(String to, String subject, String body) {
        Notification notification = Notification.builder()
                .destinataire(to)
                .sujet(subject)
                .contenu(body)
                .typeCanal(NotificationChannel.EMAIL)
                .statut(NotificationStatus.EN_ATTENTE)
                .build();

        notification = notificationRepository.save(notification);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            notification.setStatut(NotificationStatus.ENVOYE);
            notification.setSentAt(Instant.now());
            log.info("Email sent to {} — subject: '{}'", to, subject);

        } catch (MailException e) {
            notification.setStatut(NotificationStatus.ECHEC);
            notification.setErrorMessage(e.getMessage());
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }

        return notificationRepository.save(notification);
    }

    /**
     * Logs and persists an SMS notification.
     * Actual SMS gateway integration can replace the log statement.
     */
    @Transactional
    public Notification sendSms(String phoneNumber, String message) {
        Notification notification = Notification.builder()
                .destinataire(phoneNumber)
                .sujet("SMS")
                .contenu(message)
                .typeCanal(NotificationChannel.SMS)
                .statut(NotificationStatus.EN_ATTENTE)
                .build();

        notification = notificationRepository.save(notification);

        log.info("[SMS] To: {} | Message: {}", phoneNumber, message);

        notification.setStatut(NotificationStatus.ENVOYE);
        notification.setSentAt(Instant.now());
        return notificationRepository.save(notification);
    }

    /**
     * Persists an in-app notification (read from front-end via REST).
     */
    @Transactional
    public Notification createInAppNotification(String recipient, String subject, String content) {
        Notification notification = Notification.builder()
                .destinataire(recipient)
                .sujet(subject)
                .contenu(content)
                .typeCanal(NotificationChannel.IN_APP)
                .statut(NotificationStatus.ENVOYE)
                .sentAt(Instant.now())
                .build();

        return notificationRepository.save(notification);
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    public List<NotificationDto> getByRecipient(String email) {
        return notificationRepository.findByDestinataire(email)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    public Page<NotificationDto> getByRecipientPaged(String email, Pageable pageable) {
        return notificationRepository
                .findByDestinataireOrderByCreatedAtDesc(email, pageable)
                .map(NotificationDto::fromEntity);
    }

    public List<NotificationDto> getFailed() {
        return notificationRepository.findByStatut(NotificationStatus.ECHEC)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    // =========================================================================
    // RETRY FAILED EMAILS
    // =========================================================================

    /**
     * Retries all ECHEC email notifications.
     * Called by admin endpoint or a future retry scheduler.
     */
    @Transactional
    public int retryFailedEmails() {
        List<Notification> failed = notificationRepository
                .findByStatutAndTypeCanal(NotificationStatus.ECHEC, NotificationChannel.EMAIL);

        int success = 0;
        for (Notification n : failed) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(n.getDestinataire());
                message.setSubject(n.getSujet());
                message.setText(n.getContenu());
                mailSender.send(message);

                n.setStatut(NotificationStatus.ENVOYE);
                n.setSentAt(Instant.now());
                n.setErrorMessage(null);
                notificationRepository.save(n);
                success++;
                log.info("Retry succeeded for notification id={}", n.getId());

            } catch (MailException e) {
                n.setErrorMessage(e.getMessage());
                notificationRepository.save(n);
                log.warn("Retry failed for notification id={}: {}", n.getId(), e.getMessage());
            }
        }
        return success;
    }
}
