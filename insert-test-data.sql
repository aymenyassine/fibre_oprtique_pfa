-- ============================================================================
-- SCRIPT D'INSERTION DE DONNÉES DE TEST - MAROC
-- Base: fibre_optique_db
-- Mot de passe hash: $2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO
-- ============================================================================

USE fibre_optique_db;

-- Désactiver les vérifications de clés étrangères pendant l'insertion
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- ============================================================================
-- 1. UTILISATEURS (avec mot de passe "test123")
-- ============================================================================

DELETE FROM users WHERE email NOT IN ('admin@fibreoptique.com');

INSERT INTO users (nom, prenom, email, telephone, password_hash, role, enabled, email_verified, adresse, ville, code_postal) VALUES
-- Administrateurs
('Nom', 'Jean', 'admin@example.com', '+33123456789', '$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO', 'ADMIN', TRUE, TRUE, '123 Rue Admin', 'Casablanca', '20000'),

-- Techniciens
('Bouvier', 'Pierre', 'tech1@example.com', '+212612345678', '$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO', 'TECHNICIEN', TRUE, TRUE, '456 Rue Tech', 'Marrakech', '40000'),
('Martin', 'Sophie', 'tech2@example.com', '+212698765432', '$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO', 'TECHNICIEN', TRUE, TRUE, '789 Avenue Tech', 'Fès', '30000'),

-- Commerciaux
('Bernard', 'Marc', 'commercial1@example.com', '+212611223344', '$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO', 'COMMERCIAL', TRUE, TRUE, '321 Boulevard Comm', 'Rabat', '10000'),
('Moreau', 'Anne', 'commercial2@example.com', '+212655443322', '$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO', 'COMMERCIAL', TRUE, TRUE, '654 Rue Comm', 'Tanger', '90000'),

-- Clients
('Petit', 'Luc', 'client1@example.com', '+212612345671', '$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO', 'CLIENT', TRUE, TRUE, '111 Rue Client', 'Agadir', '80000'),
('Durand', 'Marie', 'client2@example.com', '+212698765433', '$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO', 'CLIENT', TRUE, TRUE, '222 Avenue Client', 'Meknès', '50000');

-- ============================================================================
-- 2. DATACENTERS (Maroc - Points stratégiques)
-- ============================================================================

DELETE FROM datacenter;

INSERT INTO datacenter (nom, capacite, coordinate) VALUES
('DC-Casablanca-Central', 500, ST_GeomFromText('POINT(-7.5898 33.5731)', 4326)),
('DC-Rabat-Gouvernement', 300, ST_GeomFromText('POINT(-6.8416 34.0209)', 4326)),
('DC-Marrakech-Sud', 250, ST_GeomFromText('POINT(-8.0029 31.6295)', 4326)),
('DC-Fès-Nord', 200, ST_GeomFromText('POINT(-5.0167 34.0333)', 4326)),
('DC-Tanger-Littoral', 150, ST_GeomFromText('POINT(-5.8080 35.7595)', 4326));

-- ============================================================================
-- 3. RÉPARTITEURS
-- ============================================================================

TRUNCATE TABLE repartiteur;

INSERT INTO repartiteur (nom, nb_ports, coordinate, datacenter_id) VALUES
-- Répartiteurs pour DC-Casablanca-Central
('REP-CASA-Centre-01', 96, ST_GeomFromText('POINT(-7.5800 33.5700)', 4326), 1),
('REP-CASA-Centre-02', 96, ST_GeomFromText('POINT(-7.5900 33.5800)', 4326), 1),
('REP-CASA-Port', 48, ST_GeomFromText('POINT(-7.6100 33.5600)', 4326), 1),

-- Répartiteurs pour DC-Rabat-Gouvernement
('REP-RABAT-01', 72, ST_GeomFromText('POINT(-6.8300 34.0200)', 4326), 2),
('REP-RABAT-02', 48, ST_GeomFromText('POINT(-6.8500 34.0300)', 4326), 2),

-- Répartiteurs pour DC-Marrakech-Sud
('REP-MARRA-Médina', 72, ST_GeomFromText('POINT(-8.0100 31.6300)', 4326), 3),
('REP-MARRA-Nouvelle', 48, ST_GeomFromText('POINT(-7.9900 31.6200)', 4326), 3),

-- Répartiteurs pour DC-Fès-Nord
('REP-FES-01', 48, ST_GeomFromText('POINT(-5.0100 34.0300)', 4326), 4),

-- Répartiteurs pour DC-Tanger-Littoral
('REP-TANGER-01', 48, ST_GeomFromText('POINT(-5.8000 35.7600)', 4326), 5);

-- ============================================================================
-- 4. SPLITTERS (ratio, nb_sortie, repartiteur_id)
-- ============================================================================

TRUNCATE TABLE splitter;

INSERT INTO splitter (ratio, nb_sortie, repartiteur_id) VALUES
-- Splitters pour REP-CASA-Centre-01
('1:32', 32, 1),
('1:32', 32, 1),
('1:32', 32, 1),

-- Splitters pour REP-CASA-Centre-02
('1:32', 32, 2),
('1:32', 32, 2),
('1:16', 16, 2),

-- Splitters pour REP-CASA-Port
('1:16', 16, 3),
('1:32', 32, 3),

-- Splitters pour REP-RABAT-01
('1:32', 32, 4),
('1:32', 32, 4),

-- Splitters pour REP-RABAT-02
('1:32', 32, 5),

-- Splitters pour REP-MARRA-Médina
('1:32', 32, 6),
('1:32', 32, 6),

-- Splitters pour REP-MARRA-Nouvelle
('1:32', 32, 7),

-- Splitters pour REP-FES-01
('1:32', 32, 8),

-- Splitters pour REP-TANGER-01
('1:32', 32, 9);

-- ============================================================================
-- 5. BOÎTES CLIENT (nom, nb_ports, ports_utilises, coordinate, splitter_id)
-- ============================================================================

TRUNCATE TABLE boite_client;

INSERT INTO boite_client (nom, nb_ports, ports_utilises, coordinate, splitter_id) VALUES
-- Boîtes pour Casablanca
('BC-CASA-RueMohammed-01', 32, 15, ST_GeomFromText('POINT(-7.5750 33.5720)', 4326), 1),
('BC-CASA-RueMohammed-02', 32, 8, ST_GeomFromText('POINT(-7.5770 33.5730)', 4326), 1),
('BC-CASA-AvenueParis', 32, 20, ST_GeomFromText('POINT(-7.5850 33.5750)', 4326), 2),
('BC-CASA-AvenueFAR', 32, 10, ST_GeomFromText('POINT(-7.5900 33.5680)', 4326), 2),
('BC-CASA-Port-01', 16, 12, ST_GeomFromText('POINT(-7.6050 33.5650)', 4326), 3),
('BC-CASA-Port-02', 32, 5, ST_GeomFromText('POINT(-7.6150 33.5600)', 4326), 3),

-- Boîtes pour Rabat
('BC-RABAT-Centre', 32, 18, ST_GeomFromText('POINT(-6.8350 34.0220)', 4326), 4),
('BC-RABAT-Gouvernement', 32, 22, ST_GeomFromText('POINT(-6.8400 34.0250)', 4326), 4),
('BC-RABAT-Agdal', 32, 7, ST_GeomFromText('POINT(-6.8300 34.0180)', 4326), 5),

-- Boîtes pour Marrakech
('BC-MARRA-Medina', 32, 14, ST_GeomFromText('POINT(-8.0100 31.6310)', 4326), 6),
('BC-MARRA-Gueliz', 32, 9, ST_GeomFromText('POINT(-7.9950 31.6250)', 4326), 6),
('BC-MARRA-Nouvelle-01', 32, 16, ST_GeomFromText('POINT(-7.9900 31.6180)', 4326), 7),

-- Boîtes pour Fès
('BC-FES-Medina', 32, 11, ST_GeomFromText('POINT(-5.0150 34.0330)', 4326), 8),

-- Boîtes pour Tanger
('BC-TANGER-Centre', 32, 13, ST_GeomFromText('POINT(-5.8050 35.7620)', 4326), 9);

-- ============================================================================
-- 6. ÉQUIPEMENTS (OLT, ONT, SWITCH, ROUTEUR, AMPLI)
-- ============================================================================

TRUNCATE TABLE equipement;

INSERT INTO equipement (nom, modele, num_serie, ip, status, type, repartiteur_id) VALUES
-- Équipements pour REP-CASA-Centre-01
('OLT-CASA-01', 'Ericsson LG8310', 'ERG-CASA-001', '192.168.1.10', 'ACTIF', 'OLT', 1),
('SWITCH-CASA-01', 'Cisco Catalyst 2960', 'CSC-CASA-001', '192.168.1.11', 'ACTIF', 'SWITCH', 1),
('AMPLI-CASA-01', 'Corning JDSU OSP', 'AMP-CASA-001', '192.168.1.12', 'ACTIF', 'AMPLI', 1),

-- Équipements pour REP-CASA-Centre-02
('OLT-CASA-02', 'Ericsson LG8310', 'ERG-CASA-002', '192.168.1.20', 'ACTIF', 'OLT', 2),
('ROUTEUR-CASA-02', 'Cisco ASR 900', 'CSC-CASA-002', '192.168.1.21', 'EN_MAINTENANCE', 'ROUTEUR', 2),

-- Équipements pour REP-CASA-Port
('OLT-CASA-PORT', 'Ericsson LG8310', 'ERG-CASA-PORT', '192.168.1.30', 'ACTIF', 'OLT', 3),

-- Équipements pour REP-RABAT-01
('OLT-RABAT-01', 'Ericsson LG8310', 'ERG-RABAT-001', '192.168.2.10', 'ACTIF', 'OLT', 4),
('SWITCH-RABAT-01', 'Cisco Catalyst 3650', 'CSC-RABAT-001', '192.168.2.11', 'ACTIF', 'SWITCH', 4),

-- Équipements pour REP-RABAT-02
('AMPLI-RABAT-02', 'Corning JDSU OSP', 'AMP-RABAT-002', '192.168.2.20', 'ACTIF', 'AMPLI', 5),

-- Équipements pour REP-MARRA-Médina
('OLT-MARRA-01', 'Ericsson LG8310', 'ERG-MARRA-001', '192.168.3.10', 'ACTIF', 'OLT', 6),

-- Équipements pour REP-MARRA-Nouvelle
('OLT-MARRA-02', 'Ericsson LG8310', 'ERG-MARRA-002', '192.168.3.20', 'INACTIF', 'OLT', 7),

-- Équipements pour REP-FES-01
('OLT-FES-01', 'Ericsson LG8310', 'ERG-FES-001', '192.168.4.10', 'ACTIF', 'OLT', 8),

-- Équipements pour REP-TANGER-01
('OLT-TANGER-01', 'Ericsson LG8310', 'ERG-TANGER-001', '192.168.5.10', 'ACTIF', 'OLT', 9);

-- ============================================================================
-- 7. CHEMINS FIBRE (source_node_id, dest_node_id, longueur, type_fibre, statut)
-- ============================================================================

TRUNCATE TABLE chemin_fibre;

INSERT INTO chemin_fibre (source_node_id, dest_node_id, longueur, type_fibre, statut) VALUES
-- Liaisons Datacenters vers Répartiteurs (longue distance - MONOMODE)
-- DC-Casablanca → REP-CASA
(1, 1, 2.5, 'MONOMODE', 'OK'),
(1, 2, 2.8, 'MONOMODE', 'OK'),
(1, 3, 3.2, 'MONOMODE', 'OK'),

-- DC-Rabat → REP-RABAT
(2, 4, 1.5, 'MONOMODE', 'OK'),
(2, 5, 1.8, 'MONOMODE', 'MAINTENANCE'),

-- DC-Marrakech → REP-MARRA
(3, 6, 2.2, 'MONOMODE', 'OK'),
(3, 7, 2.5, 'MONOMODE', 'OK'),

-- DC-Fès → REP-FES
(4, 8, 1.3, 'MONOMODE', 'OK'),

-- DC-Tanger → REP-TANGER
(5, 9, 1.1, 'MONOMODE', 'OK'),

-- Liaisons Répartiteurs vers Boîtes Client (courte distance - MULTIMODE)
-- Casablanca
(1, 1, 0.8, 'MULTIMODE', 'OK'),
(1, 2, 0.9, 'MULTIMODE', 'OK'),
(2, 3, 0.7, 'MULTIMODE', 'OK'),
(2, 4, 0.85, 'MULTIMODE', 'INCIDENT'),
(3, 5, 0.6, 'MULTIMODE', 'OK'),
(3, 6, 0.75, 'MULTIMODE', 'OK'),

-- Rabat
(4, 7, 0.8, 'MULTIMODE', 'OK'),
(4, 8, 0.85, 'MULTIMODE', 'OK'),
(5, 9, 0.7, 'MULTIMODE', 'OK'),

-- Marrakech
(6, 10, 0.9, 'MULTIMODE', 'OK'),
(6, 11, 0.85, 'MULTIMODE', 'OK'),
(7, 12, 0.8, 'MULTIMODE', 'OK'),

-- Fès
(8, 13, 0.75, 'MULTIMODE', 'OK'),

-- Tanger
(9, 14, 0.8, 'MULTIMODE', 'OK'),

-- Liaisons inter-Datacenters (MONOMODE longue distance)
(1, 2, 95.0, 'MONOMODE', 'OK'),  -- Casablanca ↔ Rabat
(1, 3, 240.0, 'MONOMODE', 'OK'), -- Casablanca ↔ Marrakech
(1, 4, 515.0, 'MONOMODE', 'MAINTENANCE'), -- Casablanca ↔ Fès
(1, 5, 660.0, 'MONOMODE', 'OK'), -- Casablanca ↔ Tanger
(2, 3, 340.0, 'MONOMODE', 'OK'), -- Rabat ↔ Marrakech
(2, 4, 420.0, 'MONOMODE', 'OK'); -- Rabat ↔ Fès

-- ============================================================================
-- 8. OFFRES (abonnements)
-- ============================================================================

TRUNCATE TABLE offre;

INSERT INTO offre (nom, description, debit_montant, debit_descendant, prix_ht, taux_tva, type_engagement, duree_mois, technologie, actif) VALUES
('Fibre 100', 'Internet FTTH 100 Mbps', 100, 100, 99.99, 20.00, 'SANS_ENGAGEMENT', 1, 'FTTH', TRUE),
('Fibre 300', 'Internet FTTH 300 Mbps', 300, 300, 149.99, 20.00, 'DOUZE_MOIS', 12, 'FTTH', TRUE),
('Fibre 500', 'Internet FTTH 500 Mbps', 500, 500, 199.99, 20.00, 'DOUZE_MOIS', 12, 'FTTH', TRUE),
('Fibre 1000', 'Internet FTTH 1000 Mbps', 1000, 1000, 299.99, 20.00, 'VINGT_QUATRE_MOIS', 24, 'FTTH', TRUE),
('Fibre Pro 300', 'Internet FTTB 300 Mbps Pro', 300, 300, 129.99, 20.00, 'DOUZE_MOIS', 12, 'FTTB', TRUE);

-- ============================================================================
-- 9. ABONNEMENTS (client_id, offre_id, date_debut, date_fin, statut)
-- ============================================================================

TRUNCATE TABLE abonnement;

INSERT INTO abonnement (client_id, offre_id, date_debut, date_fin, statut, version) VALUES
(6, 1, '2026-01-15', '2026-02-15', 'ACTIF', 0),
(6, 2, '2026-03-01', '2027-03-01', 'ACTIF', 0),
(7, 3, '2025-12-01', '2026-12-01', 'ACTIF', 0),
(7, 4, '2026-06-01', '2028-06-01', 'EN_ATTENTE', 0);

-- ============================================================================
-- 10. CONTRATS
-- ============================================================================

TRUNCATE TABLE contrat;

INSERT INTO contrat (abonnement_id, pdf_storage_key, date_signature, version) VALUES
(1, 'contrats/CONTRAT-1-20260115.pdf', NOW(), 0),
(2, 'contrats/CONTRAT-2-20260301.pdf', NOW(), 0),
(3, 'contrats/CONTRAT-3-20251201.pdf', NOW(), 0);

-- ============================================================================
-- 11. FACTEURS (invoices)
-- ============================================================================

TRUNCATE TABLE facture;

INSERT INTO facture (abonnement_id, client_id, periode_debut, periode_fin, montant_ht, taux_tva, montant_ttc, statut, date_emission) VALUES
(1, 6, '2026-01-15', '2026-02-15', 99.99, 20.00, 119.99, 'PAYEE', '2026-01-14'),
(1, 6, '2026-02-15', '2026-03-15', 99.99, 20.00, 119.99, 'PAYEE', '2026-02-14'),
(2, 6, '2026-03-01', '2026-04-01', 149.99, 20.00, 179.99, 'EN_ATTENTE', '2026-03-01'),
(3, 7, '2025-12-01', '2026-01-01', 199.99, 20.00, 239.99, 'EN_RETARD', '2025-12-01'),
(3, 7, '2026-01-01', '2026-02-01', 199.99, 20.00, 239.99, 'PAYEE', '2026-01-01'),
(4, 7, '2026-06-01', '2026-07-01', 299.99, 20.00, 359.99, 'EN_ATTENTE', '2026-06-01');

-- ============================================================================
-- 12. DEMANDES DE RACCORDEMENT
-- ============================================================================

TRUNCATE TABLE demande_raccordement;

INSERT INTO demande_raccordement (prospect_id, offre_id, nom_demandeur, email, telephone, adresse_complete, latitude, longitude, date_installation_souhaitee, statut) VALUES
(6, 1, 'Petit Luc', 'client1@example.com', '+212612345671', '111 Rue Client, Agadir 80000', 30.4278, -9.5981, '2026-07-15', 'EN_COURS'),
(7, 2, 'Durand Marie', 'client2@example.com', '+212698765433', '222 Avenue Client, Meknès 50000', 33.8869, -5.5553, '2026-08-01', 'APPROUVEE');

-- ============================================================================
-- 13. INTERVENTIONS
-- ============================================================================

TRUNCATE TABLE intervention;

INSERT INTO intervention (demande_id, technicien_id, scheduled_time, statut, check_in_time, check_out_time, latitude_check_in, longitude_check_in) VALUES
(1, 2, '2026-07-15 09:00:00', 'COMPLETEE', '2026-07-15 09:15:00', '2026-07-15 11:30:00', 30.4280, -9.5980),
(2, 2, '2026-08-01 14:00:00', 'PLANIFIEE', NULL, NULL, NULL, NULL);

-- ============================================================================
-- 14. TICKETS SUPPORT
-- ============================================================================

TRUNCATE TABLE ticket;

INSERT INTO ticket (client_id, assigned_to_id, titre, description, categorie, priorite, statut, escalation_level, created_at, last_activity_at, sla_deadline, closed_at, version) VALUES
(6, 2, 'Vitesse Internet faible', 'Ma connexion FTTH 100 est très lente depuis hier', 'CONNEXION', 'HAUTE', 'EN_COURS', 0, NOW(6), NOW(6), DATE_ADD(NOW(), INTERVAL 24 HOUR), NULL, 0),
(7, 2, 'Facturation - Double paiement', 'J''ai été facturisé deux fois en juin', 'FACTURATION', 'NORMALE', 'RESOLU', 0, NOW(6), NOW(6), DATE_ADD(NOW(), INTERVAL 48 HOUR), DATE_ADD(NOW(), INTERVAL -1 DAY), 0);

-- ============================================================================
-- 15. NOTIFICATIONS
-- ============================================================================

TRUNCATE TABLE notification;

INSERT INTO notification (destinataire, sujet, contenu, event_type, type_canal, statut, retry_count) VALUES
('client1@example.com', 'Bienvenue chez FibreOps', 'Votre abonnement Fibre 100 a été activé', 'ABONNEMENT_ACTIVE', 'EMAIL', 'ENVOYE', 0),
('client2@example.com', 'Nouvelle facturation', 'Votre facture de juin est disponible', 'FACTURE_EMISE', 'EMAIL', 'EN_ATTENTE', 0),
('admin@example.com', 'Alerte: Incident réseau', 'Liaison DC-Rabat en incident depuis 2h', 'INCIDENT_DETETE', 'EMAIL', 'ENVOYE', 0);

-- ============================================================================
-- Réactiver les vérifications de clés étrangères
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- ============================================================================
-- RÉSUMÉ DES DONNÉES INSÉRÉES
-- ============================================================================

SELECT '===== RÉSUMÉ DES DONNÉES DE TEST =====' AS Info;
SELECT CONCAT('Utilisateurs: ', COUNT(*)) FROM users;
SELECT CONCAT('Datacenters: ', COUNT(*)) FROM datacenter;
SELECT CONCAT('Répartiteurs: ', COUNT(*)) FROM repartiteur;
SELECT CONCAT('Splitters: ', COUNT(*)) FROM splitter;
SELECT CONCAT('Boîtes Clients: ', COUNT(*)) FROM boite_client;
SELECT CONCAT('Équipements: ', COUNT(*)) FROM equipement;
SELECT CONCAT('Chemins Fibre: ', COUNT(*)) FROM chemin_fibre;
SELECT CONCAT('Offres: ', COUNT(*)) FROM offre;
SELECT CONCAT('Abonnements: ', COUNT(*)) FROM abonnement;
SELECT CONCAT('Contrats: ', COUNT(*)) FROM contrat;
SELECT CONCAT('Factures: ', COUNT(*)) FROM facture;
SELECT CONCAT('Demandes Raccordement: ', COUNT(*)) FROM demande_raccordement;
SELECT CONCAT('Interventions: ', COUNT(*)) FROM intervention;
SELECT CONCAT('Tickets Support: ', COUNT(*)) FROM ticket;
SELECT CONCAT('Notifications: ', COUNT(*)) FROM notification;
SELECT '=====================================';

-- ============================================================================
-- ACCÈS DE TEST
-- ============================================================================
-- Email: admin@example.com | Motdepasse: test123
-- Email: tech1@example.com | Motdepasse: test123
-- Email: tech2@example.com | Motdepasse: test123
-- Email: commercial1@example.com | Motdepasse: test123
-- Email: commercial2@example.com | Motdepasse: test123
-- Email: client1@example.com | Motdepasse: test123
-- Email: client2@example.com | Motdepasse: test123
-- ============================================================================
