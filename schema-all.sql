-- ==============================================================================
-- FIBRE OPTIQUE PLATFORM - UNIFIED SQL SCHEMA (SINGLE DATABASE)
-- Toutes les tables des anciens microservices (auth_db, user_db, network_db,
-- offer_db, subscription_db, request_db, billing_db, support_db,
-- notification_db) sont regroupées ici dans UNE seule base : fibre_optique_db.
--
-- Principes appliqués :
--   - Une seule table "outbox" commune (au lieu de 5 tables identiques)
--   - Toutes les FK inter-domaines, auparavant implicites (simple BIGINT sans
--     contrainte car elles pointaient vers une autre base), sont maintenant
--     déclarées explicitement (users, offre, abonnement, demande_raccordement...)
--   - Tous les index originaux sont conservés (et quelques-uns ajoutés sur les
--     nouvelles FK pour garder de bonnes perfs de jointure)
--   - Ordre de création des tables respecte les dépendances de clés étrangères
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS fibre_optique_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE fibre_optique_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS processed_event;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS outbox;
DROP TABLE IF EXISTS message_ticket;
DROP TABLE IF EXISTS ticket;
DROP TABLE IF EXISTS facture;
DROP TABLE IF EXISTS intervention;
DROP TABLE IF EXISTS demande_raccordement;
DROP TABLE IF EXISTS contrat;
DROP TABLE IF EXISTS abonnement;
DROP TABLE IF EXISTS offre;
DROP TABLE IF EXISTS chemin_fibre;
DROP TABLE IF EXISTS boite_client;
DROP TABLE IF EXISTS splitter;
DROP TABLE IF EXISTS equipement;
DROP TABLE IF EXISTS repartiteur;
DROP TABLE IF EXISTS datacenter;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS login_audit_logs;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================
-- 1. UTILISATEURS & AUTHENTIFICATION
-- (anciennement: user_db.users, auth_db.login_audit_logs, auth_db.refresh_tokens)
-- ==============================================================================

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    email VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','TECHNICIEN','CLIENT','PROSPECT','COMMERCIAL','SUPPORT') NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    adresse VARCHAR(255),
    ville VARCHAR(100),
    code_postal VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_enabled (enabled),
    INDEX idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE login_audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    email VARCHAR(255) NOT NULL,
    result VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_login_audit_logs_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_login_audit_logs_user_id ON login_audit_logs (user_id);
CREATE INDEX idx_login_audit_logs_email ON login_audit_logs (email);
CREATE INDEX idx_login_audit_logs_timestamp ON login_audit_logs (timestamp);

CREATE TABLE refresh_tokens (
    id BINARY(16) NOT NULL, -- UUID mappé en BINARY(16) par Hibernate/MySQL
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked TINYINT(1) NOT NULL DEFAULT 0,
    replaced_by_token VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token_hash (token_hash),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

-- Default password: admin123 (BCrypt encoded)
INSERT INTO users (nom, prenom, email, telephone, password_hash, role, enabled, email_verified, adresse, ville, code_postal)
VALUES ('Administrateur', 'Système', 'admin@fibreoptique.com', '+33123456789',
        '$2a$10$rKOqQw8Vz1mKkZq7KqWzKOkQqQw8Vz1mKkZq7KqWzKOkQqQw8Vz1m',
        'ADMIN', TRUE, TRUE, 'Siège Social', 'Paris', '75001')
ON DUPLICATE KEY UPDATE email = email;


-- ==============================================================================
-- 2. RÉSEAU PHYSIQUE (anciennement: network_db)
-- ==============================================================================

CREATE TABLE datacenter (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    nom         VARCHAR(100)    NOT NULL,
    capacite    INT             NOT NULL,
    coordinate  POINT           NOT NULL SRID 4326,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE SPATIAL INDEX idx_datacenter_coordinate ON datacenter (coordinate);
CREATE INDEX idx_datacenter_nom ON datacenter (nom);

CREATE TABLE repartiteur (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(100)    NOT NULL,
    nb_ports        INT             NOT NULL,
    coordinate      POINT           NOT NULL SRID 4326,
    datacenter_id   BIGINT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_repartiteur_datacenter
        FOREIGN KEY (datacenter_id) REFERENCES datacenter (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE SPATIAL INDEX idx_repartiteur_coordinate ON repartiteur (coordinate);
CREATE INDEX idx_repartiteur_datacenter_id ON repartiteur (datacenter_id);
CREATE INDEX idx_repartiteur_nom ON repartiteur (nom);

CREATE TABLE equipement (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(100)    NOT NULL,
    modele          VARCHAR(100)    NOT NULL,
    num_serie       VARCHAR(100)    NOT NULL,
    ip              VARCHAR(45)     NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    repartiteur_id  BIGINT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_equipement_num_serie UNIQUE (num_serie),
    CONSTRAINT fk_equipement_repartiteur
        FOREIGN KEY (repartiteur_id) REFERENCES repartiteur (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_equipement_repartiteur_id ON equipement (repartiteur_id);
CREATE INDEX idx_equipement_status ON equipement (status);
CREATE INDEX idx_equipement_type ON equipement (type);
CREATE INDEX idx_equipement_status_type ON equipement (status, type);

CREATE TABLE splitter (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    ratio           VARCHAR(20)     NOT NULL,
    nb_sortie       INT             NOT NULL,
    repartiteur_id  BIGINT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_splitter_repartiteur
        FOREIGN KEY (repartiteur_id) REFERENCES repartiteur (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_splitter_repartiteur_id ON splitter (repartiteur_id);
CREATE INDEX idx_splitter_ratio ON splitter (ratio);

CREATE TABLE boite_client (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(100)    NOT NULL,
    nb_ports        INT             NOT NULL,
    ports_utilises  INT             NOT NULL DEFAULT 0,
    coordinate      POINT           NOT NULL SRID 4326,
    splitter_id     BIGINT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_boite_client_splitter
        FOREIGN KEY (splitter_id) REFERENCES splitter (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_boite_client_ports CHECK (ports_utilises >= 0 AND ports_utilises <= nb_ports)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE SPATIAL INDEX idx_boite_client_coordinate ON boite_client (coordinate);
CREATE INDEX idx_boite_client_splitter_id ON boite_client (splitter_id);
CREATE INDEX idx_boite_client_available_ports ON boite_client (ports_utilises, nb_ports);
CREATE INDEX idx_boite_client_nom ON boite_client (nom);

CREATE TABLE chemin_fibre (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    source_node_id  BIGINT          NOT NULL,
    dest_node_id    BIGINT          NOT NULL,
    longueur        DOUBLE          NOT NULL,
    type_fibre      VARCHAR(20)     NOT NULL,
    statut          VARCHAR(30)     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_chemin_fibre_longueur CHECK (longueur > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_chemin_fibre_source ON chemin_fibre (source_node_id);
CREATE INDEX idx_chemin_fibre_dest   ON chemin_fibre (dest_node_id);
CREATE INDEX idx_chemin_fibre_source_dest ON chemin_fibre (source_node_id, dest_node_id);
CREATE INDEX idx_chemin_fibre_statut ON chemin_fibre (statut);
CREATE INDEX idx_chemin_fibre_type ON chemin_fibre (type_fibre);


-- ==============================================================================
-- 3. CATALOGUE D'OFFRES (anciennement: offer_db)
-- ==============================================================================

CREATE TABLE offre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    debit_montant INT NOT NULL,
    debit_descendant INT NOT NULL,
    prix_ht DECIMAL(10,2) NOT NULL,
    taux_tva DECIMAL(4,2) NOT NULL,
    type_engagement ENUM('SANS_ENGAGEMENT', 'DOUZE_MOIS', 'VINGT_QUATRE_MOIS') NOT NULL,
    duree_mois INT NOT NULL,
    technologie ENUM('FTTH', 'FTTB') NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_debit_montant CHECK (debit_montant > 0),
    CONSTRAINT chk_debit_descendant CHECK (debit_descendant > 0),
    CONSTRAINT chk_prix_ht CHECK (prix_ht > 0),
    CONSTRAINT chk_duree_mois CHECK (duree_mois >= 0),
    CONSTRAINT chk_taux_tva CHECK (taux_tva >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_offre_actif ON offre (actif);
CREATE INDEX idx_offre_technologie ON offre (technologie);


-- ==============================================================================
-- 4. ABONNEMENTS & CONTRATS (anciennement: subscription_db)
-- ==============================================================================

CREATE TABLE abonnement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    offre_id BIGINT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut VARCHAR(50) NOT NULL,
    date_changement_offre DATETIME NULL,
    created_at DATETIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_abonnement_client
        FOREIGN KEY (client_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_abonnement_offre
        FOREIGN KEY (offre_id) REFERENCES offre (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_abonnement_client_id ON abonnement (client_id);
CREATE INDEX idx_abonnement_offre_id ON abonnement (offre_id);
CREATE INDEX idx_abonnement_statut ON abonnement (statut);

CREATE TABLE contrat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    abonnement_id BIGINT NOT NULL,
    pdf_storage_key VARCHAR(255) NOT NULL,
    date_signature DATETIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_contrat_abonnement
        FOREIGN KEY (abonnement_id) REFERENCES abonnement (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_contrat_abonnement_id ON contrat (abonnement_id);


-- ==============================================================================
-- 5. DEMANDES DE RACCORDEMENT & INTERVENTIONS (anciennement: request_db)
-- ==============================================================================

CREATE TABLE demande_raccordement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prospect_id BIGINT NOT NULL,
    offre_id BIGINT NOT NULL,
    nom_demandeur VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    adresse_complete VARCHAR(500) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    date_installation_souhaitee DATE NOT NULL,
    statut VARCHAR(50) NOT NULL,
    commentaire_traitement TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_demande_prospect
        FOREIGN KEY (prospect_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_demande_offre
        FOREIGN KEY (offre_id) REFERENCES offre (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_latitude CHECK (latitude IS NULL OR (latitude >= -90.0 AND latitude <= 90.0)),
    CONSTRAINT chk_longitude CHECK (longitude IS NULL OR (longitude >= -180.0 AND longitude <= 180.0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_demande_prospect ON demande_raccordement (prospect_id);
CREATE INDEX idx_demande_offre_id ON demande_raccordement (offre_id);
CREATE INDEX idx_demande_statut ON demande_raccordement (statut);

CREATE TABLE intervention (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demande_id BIGINT NOT NULL,
    technicien_id BIGINT NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    statut VARCHAR(50) NOT NULL,
    check_in_time TIMESTAMP NULL,
    check_out_time TIMESTAMP NULL,
    latitude_check_in DOUBLE,
    longitude_check_in DOUBLE,
    notes TEXT,
    CONSTRAINT fk_intervention_demande
        FOREIGN KEY (demande_id) REFERENCES demande_raccordement (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_intervention_technicien
        FOREIGN KEY (technicien_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_latitude_check_in CHECK (latitude_check_in IS NULL OR (latitude_check_in >= -90.0 AND latitude_check_in <= 90.0)),
    CONSTRAINT chk_longitude_check_in CHECK (longitude_check_in IS NULL OR (longitude_check_in >= -180.0 AND longitude_check_in <= 180.0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_intervention_demande ON intervention (demande_id);
CREATE INDEX idx_intervention_technicien ON intervention (technicien_id);


-- ==============================================================================
-- 6. FACTURATION (anciennement: billing_db)
-- ==============================================================================

CREATE TABLE facture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    abonnement_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    periode_debut DATE NOT NULL,
    periode_fin DATE NOT NULL,
    montant_ht DECIMAL(10, 2) NOT NULL,
    taux_tva DECIMAL(4, 2) NOT NULL,
    montant_ttc DECIMAL(10, 2) NOT NULL,
    statut VARCHAR(20) NOT NULL,
    date_emission TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_paiement TIMESTAMP NULL,
    pdf_storage_key VARCHAR(255) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_facture_abonnement
        FOREIGN KEY (abonnement_id) REFERENCES abonnement (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_facture_client
        FOREIGN KEY (client_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_facture_statut CHECK (statut IN ('EN_ATTENTE', 'PAYEE', 'EN_RETARD', 'ANNULEE')),
    CONSTRAINT chk_facture_period CHECK (periode_debut < periode_fin),
    CONSTRAINT chk_facture_montant CHECK (montant_ht > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_facture_client_id ON facture (client_id);
CREATE INDEX idx_facture_statut ON facture (statut);
CREATE INDEX idx_facture_date_emission ON facture (date_emission);
CREATE INDEX idx_facture_abonnement_id ON facture (abonnement_id);


-- ==============================================================================
-- 7. SUPPORT CLIENT (anciennement: support_db)
-- ==============================================================================

CREATE TABLE ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    assigned_to_id BIGINT NULL,
    titre VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    categorie VARCHAR(50) NOT NULL,
    priorite VARCHAR(50) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    escalation_level INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    last_activity_at DATETIME(6) NOT NULL,
    sla_deadline DATETIME(6) NOT NULL,
    closed_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ticket_client
        FOREIGN KEY (client_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_assigned_to
        FOREIGN KEY (assigned_to_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_ticket_categorie CHECK (categorie IN ('CONNEXION', 'FACTURATION', 'TECHNIQUE', 'AUTRE')),
    CONSTRAINT chk_ticket_priorite CHECK (priorite IN ('BASSE', 'NORMALE', 'HAUTE', 'URGENTE')),
    CONSTRAINT chk_ticket_statut CHECK (statut IN ('OUVERT', 'EN_COURS', 'RESOLU', 'FERME')),
    CONSTRAINT chk_ticket_escalation_level CHECK (escalation_level >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ticket_client_id ON ticket (client_id);
CREATE INDEX idx_ticket_assigned_to_id ON ticket (assigned_to_id);
CREATE INDEX idx_ticket_statut ON ticket (statut);

CREATE TABLE message_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    auteur_id BIGINT NOT NULL,
    contenu TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_message_ticket_ticket
        FOREIGN KEY (ticket_id) REFERENCES ticket (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_message_ticket_auteur
        FOREIGN KEY (auteur_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_message_ticket_ticket_id ON message_ticket (ticket_id);
CREATE INDEX idx_message_ticket_auteur_id ON message_ticket (auteur_id);


-- ==============================================================================
-- 8. NOTIFICATIONS (anciennement: notification_db)
-- ==============================================================================

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    destinataire VARCHAR(255) NOT NULL,
    sujet VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    event_type VARCHAR(50) NULL,
    type_canal VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_notification_channel CHECK (type_canal IN ('EMAIL', 'SMS', 'IN_APP')),
    CONSTRAINT chk_notification_statut CHECK (statut IN ('EN_ATTENTE', 'ENVOYE', 'ECHEC')),
    CONSTRAINT chk_notification_retry_count CHECK (retry_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notification_destinataire ON notification (destinataire);
CREATE INDEX idx_notification_event_type ON notification (event_type);
CREATE INDEX idx_notification_statut ON notification (statut);
CREATE INDEX idx_notification_created_at ON notification (created_at);

CREATE TABLE processed_event (
    event_id VARCHAR(100) NOT NULL PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 9. OUTBOX UNIFIÉE (fusion des 4 tables outbox: subscription, request,
--    billing, support — désormais une seule table commune pour tous les
--    domaines métier, distingués par la colonne aggregate_type)
-- ==============================================================================

CREATE TABLE outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    domaine VARCHAR(30) NOT NULL DEFAULT 'GENERIC',
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_id VARCHAR(36) NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_outbox_event_id (event_id),
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    CONSTRAINT chk_outbox_retry_count CHECK (retry_count >= 0),
    CONSTRAINT chk_outbox_domaine CHECK (domaine IN ('GENERIC', 'SUBSCRIPTION', 'REQUEST', 'BILLING', 'SUPPORT', 'NETWORK', 'NOTIFICATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_outbox_status_retry ON outbox (status, retry_count);
CREATE INDEX idx_outbox_domaine ON outbox (domaine);
CREATE INDEX idx_outbox_aggregate ON outbox (aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_created_at ON outbox (created_at);

-- ==============================================================================
-- FIN DU SCHÉMA UNIFIÉ
-- ==============================================================================