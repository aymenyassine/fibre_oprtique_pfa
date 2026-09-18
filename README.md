# Fibre Optique – Gestion de Réseau FTTH (PFA)

Backend Spring Boot pour la gestion d'un réseau de fibre optique (FTTH) : datacenters, répartiteurs, équipements, splitters, boîtes clients, chemins de fibre, contrats et utilisateurs, avec cartographie géospatiale et génération de documents PDF.

## Fonctionnalités principales

- Authentification et autorisation par rôle (JWT) : ADMIN / TECHNICIEN
- Gestion des entités réseau : datacenters, répartiteurs, équipements, splitters, boîtes clients, chemins de fibre
- Suivi des contrats clients (stockage et génération de documents PDF avec OpenPDF)
- Données géospatiales (Hibernate Spatial + JTS) pour la cartographie du réseau
- Notifications par email (Spring Mail)
- Jeux de données de test SQL fournis pour une prise en main rapide

## Stack technique

- Java 21, Spring Boot 4.1
- Spring Data JPA, Spring Security, JWT (jjwt)
- MySQL, Hibernate Spatial / JTS (données géospatiales)
- OpenPDF (génération de contrats PDF)
- Maven

## Documentation incluse dans le repo

- `SPRING_BOOT_MONOLITH_DEVELOPMENT_GUIDE.md` : guide d'architecture du projet
- `frontend-quide.md` : guide d'intégration frontend
- `INSERT-TEST-DATA-README.md` + scripts SQL : jeu de données de test

## Lancer le projet

```bash
./mvnw spring-boot:run
```

## Contexte

Projet de fin d'année (PFA) réalisé en binôme à l'EMSI Casablanca, sur la gestion d'un réseau de fibre optique (FTTH).

