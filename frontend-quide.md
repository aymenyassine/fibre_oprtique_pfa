Voici une documentation complète au format Markdown pour les endpoints de l'API. Cette documentation inclut tous les endpoints, les DTOs de requête et de réponse, ainsi que les rôles requis.

# API Documentation - Fibre Optique

## Base URL
```
/api/v1
```

---

## Authentication & Authorization

### Roles
- **ADMIN**: Full access
- **COMMERCIAL**: Sales and client management
- **TECHNICIEN**: Network and installation management
- **SUPPORT**: Support and ticket management
- **CLIENT**: End users with subscriptions
- **PROSPECT**: Potential clients

---

## 1. Authentication API
**Base Path:** `/api/v1/auth`

### 1.1 Register (Public Self-Registration)
**POST** `/register`

**Request Body:**
```tson
{
  "nom": "string",          // required, max 100
  "prenom": "string",       // required, max 100
  "email": "string",        // required, valid email format, max 150
  "password": "string"      // required, min 8 characters
}
```

**Response (201 Created):**
```tson
{
  "accessToken": "string",
  "refreshToken": "string"
}
```

### 1.2 Login
**POST** `/login`

**Request Body:**
```tson
{
  "email": "string",        // required, valid email format
  "password": "string"      // required
}
```

**Response (200 OK):**
```tson
{
  "accessToken": "string",
  "refreshToken": "string"
}
```

### 1.3 Refresh Token
**POST** `/refresh`

**Request Body:**
```tson
{
  "refreshToken": "string"  // required
}
```

**Response (200 OK):**
```tson
{
  "accessToken": "string",
  "refreshToken": "string"
}
```

### 1.4 Logout
**POST** `/logout`

**Request Body:**
```tson
{
  "refreshToken": "string"  // required
}
```

**Response:** 204 No Content

---

## 2. Offers API
**Base Path:** `/api/v1/offers`

### 2.1 Get Active Offers (Public Catalogue)
**GET** `/`

**Query Parameters:**
- `technologie` (optional): `FTTH`, `FTTB`
- `typeEngagement` (optional): `SANS_ENGAGEMENT`, `DOUZE_MOIS`, `VINGT_QUATRE_MOIS`

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "nom": "string",
    "description": "string",
    "debitMontant": 1000,
    "debitDescendant": 500,
    "prixHT": 29.99,
    "tauxTVA": 20.00,
    "prixTTC": 35.99,
    "typeEngagement": "SANS_ENGAGEMENT",
    "dureeMois": 0,
    "technologie": "FTTH",
    "actif": true
  }
]
```

### 2.2 Get All Offers (Admin Only)
**GET** `/all`

**Response:** Array of `OfferDto`

### 2.3 Get Offer by ID
**GET** `/{id}`

**Response (200 OK):** `OfferDto`

### 2.4 Get Offer Summary
**GET** `/{id}/summary`

**Response (200 OK):**
```tson
{
  "id": 1,
  "nom": "string",
  "debitDescendant": 500,
  "prixHT": 29.99,
  "prixTTC": 35.99,
  "typeEngagement": "SANS_ENGAGEMENT",
  "technologie": "FTTH"
}
```

### 2.5 Create Offer (Admin Only)
**POST** `/`

**Request Body:**
```tson
{
  "nom": "string",          // required, max 100
  "description": "string",
  "debitMontant": 1000,     // required, min 1
  "debitDescendant": 500,   // required, min 1
  "prixHT": 29.99,          // required, min 0.01
  "tauxTVA": 20.00,         // min 0, max 100
  "typeEngagement": "SANS_ENGAGEMENT", // required
  "dureeMois": 0,           // min 0
  "technologie": "FTTH",    // required
  "actif": true
}
```

**Response (201 Created):** `OfferDto`

### 2.6 Update Offer (Admin Only)
**PUT** `/{id}`

**Request Body (All fields optional - partial update):**
```tson
{
  "nom": "string",          // max 100
  "description": "string",
  "debitMontant": 1000,     // min 1
  "debitDescendant": 500,   // min 1
  "prixHT": 29.99,          // min 0.01
  "tauxTVA": 20.00,         // min 0, max 100
  "typeEngagement": "SANS_ENGAGEMENT",
  "dureeMois": 0,           // min 0
  "technologie": "FTTH",
  "actif": true
}
```

**Response (200 OK):** `OfferDto`

### 2.7 Deactivate Offer (Admin Only)
**PATCH** `/{id}/deactivate`

**Response:** 204 No Content

### 2.8 Delete Offer (Admin Only)
**DELETE** `/{id}`

**Response:** 204 No Content

---

## 3. Subscription API
**Base Path:** `/api/v1/subscriptions`

### 3.1 Get All Subscriptions (Staff Only)
**GET** `/`

**Roles:** ADMIN, COMMERCIAL, SUPPORT

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "clientId": 1,
    "clientNom": "string",
    "clientPrenom": "string",
    "clientEmail": "string",
    "offre": {
      "id": 1,
      "nom": "string",
      "debitDescendant": 500,
      "prixHT": 29.99,
      "prixTTC": 35.99,
      "typeEngagement": "SANS_ENGAGEMENT",
      "technologie": "FTTH"
    },
    "dateDebut": "2024-01-01",
    "dateFin": null,
    "statut": "ACTIF",
    "dateChangementOffre": null
  }
]
```

### 3.2 Get Subscription by ID
**GET** `/{id}`

**Access:** Own subscription only (CLIENT) or all (Staff)

**Response (200 OK):** `AbonnementDto`

### 3.3 Get Client Subscriptions
**GET** `/client/{clientId}`

**Access:** Own subscriptions (CLIENT) or all (Staff)

**Response (200 OK):** Array of `AbonnementDto`

### 3.4 Create Subscription (Staff Only)
**POST** `/`

**Roles:** COMMERCIAL, ADMIN

**Request Body:**
```tson
{
  "clientId": 1,    // required
  "offreId": 1      // required
}
```

**Response (201 Created):** `AbonnementDto`

### 3.5 Suspend Subscription
**PUT** `/{id}/suspend`

**Roles:** ADMIN, COMMERCIAL, SUPPORT

**Response (200 OK):** `AbonnementDto`

### 3.6 Terminate Subscription
**PUT** `/{id}/resilie`

**Roles:** ADMIN, COMMERCIAL

**Response (200 OK):** `AbonnementDto`

### 3.7 Reactivate Subscription
**PUT** `/{id}/reactivate`

**Roles:** ADMIN, COMMERCIAL

**Response (200 OK):** `AbonnementDto`

### 3.8 Change Offer
**PUT** `/{id}/change-offer`

**Roles:** ADMIN, COMMERCIAL

**Request Body:**
```tson
{
  "newOffreId": 2   // required
}
```

**Response (200 OK):** `AbonnementDto`

### 3.9 Get Contract
**GET** `/{id}/contract`

**Access:** Own contract (CLIENT) or all (Staff)

**Response (200 OK):**
```tson
{
  "id": 1,
  "abonnementId": 1,
  "pdfStorageKey": "string",
  "dateSignature": "2024-01-01T00:00:00Z"
}
```

### 3.10 Download Contract
**GET** `/{id}/contract/download`

**Access:** Own contract (CLIENT) or all (Staff)

**Response:** Binary PDF file (Octet-Stream)

---

## 4. Billing API
**Base Path:** `/api/v1/billing`

### 4.1 Get My Invoices
**GET** `/my-invoices`

**Roles:** CLIENT

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "reference": "FIBRE-2024-001",
    "clientId": 1,
    "clientNom": "string string",
    "clientEmail": "string",
    "abonnementId": 1,
    "offreNom": "string",
    "periodeDebut": "2024-01-01",
    "periodeFin": "2024-02-01",
    "montantHT": 29.99,
    "tauxTva": 20.00,
    "montantTTC": 35.99,
    "statut": "PAYEE",
    "dateEmission": "2024-01-01",
    "dateEcheance": "2024-01-15",
    "datePaiement": "2024-01-10T00:00:00Z",
    "pdfStorageKey": "string"
  }
]
```

### 4.2 Get All Invoices (Staff Only)
**GET** `/invoices`

**Roles:** ADMIN, COMMERCIAL, SUPPORT

**Query Parameters:**
- `clientId` (optional): number
- `statut` (optional): `EN_ATTENTE`, `PAYEE`, `EN_RETARD`, `ANNULEE`
- `from` (optional): date (ISO format)
- `to` (optional): date (ISO format)
- `page` (default: 0): number
- `size` (default: 20): number

**Response:** Page of `FactureDto`

### 4.3 Get Invoice by ID
**GET** `/invoices/{id}`

**Access:** Own invoice (CLIENT) or all (Staff)

**Response (200 OK):** `FactureDto`

### 4.4 Download Invoice PDF
**GET** `/invoices/{id}/download`

**Access:** Own invoice (CLIENT) or all (Staff)

**Response:** Binary PDF file

### 4.5 Pay Invoice
**POST** `/invoices/{id}/pay`

**Access:** Own invoice (CLIENT) or all (Staff)

**Request Body:**
```tson
{
  "montant": 35.99,         // required, min 0.01
  "referenceExterne": "string" // optional
}
```

**Response (200 OK):** `FactureDto`

### 4.6 Cancel Invoice
**PATCH** `/invoices/{id}/cancel`

**Roles:** ADMIN, COMMERCIAL

**Response (200 OK):** `FactureDto`

### 4.7 Get Billing Stats
**GET** `/stats`

**Roles:** ADMIN, COMMERCIAL

**Response (200 OK):**
```tson
{
  "totalFactures": 100,
  "facturesEnAttente": 20,
  "facturesPayees": 70,
  "facturesEnRetard": 8,
  "facturesAnnulees": 2,
  "chiffreAffairesMoisCourant": 2500.00,
  "montantImpayeTotal": 500.00
}
```

### 4.8 Trigger Monthly Invoice Generation
**POST** `/admin/generate-invoices`

**Role:** ADMIN

**Response:** 200 OK

### 4.9 Trigger Overdue Check
**POST** `/admin/check-overdue`

**Role:** ADMIN

**Response:** 200 OK

---

## 5. Requests (Raccordement) API
**Base Path:** `/api/v1/requests`

### 5.1 Get All Requests (Staff Only)
**GET** `/`

**Roles:** ADMIN, COMMERCIAL, TECHNICIEN, SUPPORT

**Query Parameters:**
- `statut` (optional): `SOUMISE`, `EN_ANALYSE`, `DEVIS_ENVOYE`, `ACCEPTEE`, `PLANIFIEE`, `TERMINE`, `REJETEE`
- `keyword` (optional): string (search in name, email, address)
- `page` (default: 0): number
- `size` (default: 20): number

**Response:** Page of `DemandeRaccordementDto`

### 5.2 Get Request by ID
**GET** `/{id}`

**Roles:** ADMIN, COMMERCIAL, TECHNICIEN, SUPPORT

**Response (200 OK):**
```tson
{
  "id": 1,
  "prospectNom": "string",
  "prospectPrenom": "string",
  "prospectEmail": "string",
  "prospectTelephone": "string",
  "adresseRaccordement": "string",
  "statut": "SOUMISE",
  "technologieDisponible": "FTTH",
  "montantDevis": 99.99,
  "datePlanification": "2024-02-01T10:00:00Z",
  "technicienId": 5,
  "technicienNom": "string string",
  "createdAt": "2024-01-01T00:00:00Z",
  "clientCreatedId": null,
  "offreIdChoisie": 1
}
```

### 5.3 Submit Request (Public)
**POST** `/`

**Request Body:**
```tson
{
  "prospectNom": "string",        // required, max 100
  "prospectPrenom": "string",     // required, max 100
  "prospectEmail": "string",      // required, valid email, max 150
  "prospectTelephone": "string",  // required, phone format
  "adresseRaccordement": "string", // required
  "longitude": 2.3488,            // optional, -180 to 180
  "latitude": 48.8534             // optional, -90 to 90
}
```

**Response (201 Created):** `DemandeRaccordementDto`

### 5.4 Set Devis
**PUT** `/{id}/devis`

**Roles:** COMMERCIAL, ADMIN

**Request Body:**
```tson
{
  "montantDevis": 99.99,  // required, min 0
  "offreId": 1            // required
}
```

**Response (200 OK):** `DemandeRaccordementDto`

### 5.5 Accept Devis
**PUT** `/{id}/accept`

**Access:** Staff or matching prospect email

**Response (200 OK):** `DemandeRaccordementDto`

### 5.6 Schedule Installation
**PUT** `/{id}/schedule`

**Roles:** ADMIN, COMMERCIAL

**Request Body:**
```tson
{
  "technicienId": 5,        // required
  "datePlanification": "2024-02-01T10:00:00Z" // required, future date
}
```

**Response (200 OK):** `DemandeRaccordementDto`

### 5.7 Complete Installation
**PUT** `/{id}/complete`

**Roles:** TECHNICIEN, ADMIN

**Response (200 OK):** `DemandeRaccordementDto`

### 5.8 Reject Request
**PUT** `/{id}/reject`

**Roles:** ADMIN, COMMERCIAL

**Request Body:**
```tson
{
  "raison": "string"  // optional
}
```

**Response (200 OK):** `DemandeRaccordementDto`

---

## 6. Network API
**Base Path:** `/api/v1/network`

### 6.1 Check Eligibility (Public)
**GET** `/eligibility`

**Query Parameters:**
- `longitude` (required): double
- `latitude` (required): double

**Response (200 OK):**
```tson
{
  "eligible": true,
  "technologieDisponible": "FTTH",
  "boiteClientNom": "string",
  "distanceMetres": 150.5,
  "message": "string"
}
```

### 6.2 Get Network Status
**GET** `/status`

**Roles:** ADMIN, TECHNICIEN, COMMERCIAL

**Response (200 OK):**
```tson
{
  "totalDatacenters": 5,
  "totalRepartiteurs": 20,
  "totalSplitters": 50,
  "totalEquipements": 100,
  "totalBoitesClient": 500,
  "boitesAvecPortsLibres": 400,
  "totalCheminsFibre": 30,
  "cheminsEnIncident": 2,
  "activeIncidents": [
    {
      "id": 1,
      "sourceNodeId": 1,
      "destNodeId": 2,
      "longueur": 5.2,
      "typeFibre": "MONOMODE",
      "statut": "INCIDENT"
    }
  ]
}
```

---

## 7. Datacenter API
**Base Path:** `/api/v1/network/datacenters`

### 7.1 Get All Datacenters
**GET** `/`

**Roles:** ADMIN, TECHNICIEN, COMMERCIAL

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "nom": "string",
    "capacite": 100,
    "longitude": 2.3488,
    "latitude": 48.8534
  }
]
```

### 7.2 Get Datacenter by ID
**GET** `/{id}`

**Roles:** ADMIN, TECHNICIEN, COMMERCIAL

**Response (200 OK):** `DatacenterDto`

### 7.3 Create Datacenter
**POST** `/`

**Role:** ADMIN

**Request Body:**
```tson
{
  "nom": "string",         // required
  "capacite": 100,         // required, min 1
  "longitude": 2.3488,     // required, -180 to 180
  "latitude": 48.8534      // required, -90 to 90
}
```

**Response (201 Created):** `DatacenterDto`

### 7.4 Update Datacenter
**PUT** `/{id}`

**Role:** ADMIN

**Request Body:** Same as Create

**Response (200 OK):** `DatacenterDto`

### 7.5 Delete Datacenter
**DELETE** `/{id}`

**Role:** ADMIN

**Response:** 204 No Content

---

## 8. Repartiteur API
**Base Path:** `/api/v1/network/repartiteurs`

### 8.1 Get All Repartiteurs
**GET** `/`

**Roles:** ADMIN, TECHNICIEN, COMMERCIAL

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "nom": "string",
    "nbPorts": 24,
    "longitude": 2.3488,
    "latitude": 48.8534,
    "datacenterId": 1,
    "datacenterNom": "string"
  }
]
```

### 8.2 Get Repartiteur by ID
**GET** `/{id}`

**Roles:** ADMIN, TECHNICIEN, COMMERCIAL

**Response (200 OK):** `RepartiteurDto`

### 8.3 Create Repartiteur
**POST** `/`

**Role:** ADMIN

**Request Body:**
```tson
{
  "nom": "string",         // required
  "nbPorts": 24,           // required, min 1
  "longitude": 2.3488,     // required
  "latitude": 48.8534,     // required
  "datacenterId": 1        // required
}
```

**Response (201 Created):** `RepartiteurDto`

### 8.4 Update Repartiteur
**PUT** `/{id}`

**Role:** ADMIN

**Response (200 OK):** `RepartiteurDto`

### 8.5 Delete Repartiteur
**DELETE** `/{id}`

**Role:** ADMIN

**Response:** 204 No Content

---

## 9. Splitter API
**Base Path:** `/api/v1/network/splitters`

### 9.1 Get All Splitters
**GET** `/`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "ratio": "1:32",
    "nbSortie": 32,
    "repartiteurId": 1,
    "repartiteurNom": "string"
  }
]
```

### 9.2 Get Splitter by ID
**GET** `/{id}`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):** `SplitterDto`

### 9.3 Create Splitter
**POST** `/`

**Role:** ADMIN

**Request Body:**
```tson
{
  "ratio": "1:32",     // required, format: 1:2|4|8|16|32|64|128
  "nbSortie": 32,      // required, min 1
  "repartiteurId": 1   // required
}
```

**Response (201 Created):** `SplitterDto`

### 9.4 Update Splitter
**PUT** `/{id}`

**Role:** ADMIN

**Response (200 OK):** `SplitterDto`

### 9.5 Delete Splitter
**DELETE** `/{id}`

**Role:** ADMIN

**Response:** 204 No Content

---

## 10. Boite Client API
**Base Path:** `/api/v1/network/boites-client`

### 10.1 Get All Boites Client
**GET** `/`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "nom": "string",
    "nbPorts": 8,
    "portsUtilises": 3,
    "portsDisponibles": 5,
    "longitude": 2.3488,
    "latitude": 48.8534,
    "splitterId": 1,
    "splitterRatio": "1:32"
  }
]
```

### 10.2 Get Boite Client by ID
**GET** `/{id}`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):** `BoiteClientDto`

### 10.3 Create Boite Client
**POST** `/`

**Role:** ADMIN

**Request Body:**
```tson
{
  "nom": "string",         // required
  "nbPorts": 8,            // required, min 1
  "portsUtilises": 0,      // min 0
  "longitude": 2.3488,     // required, -180 to 180
  "latitude": 48.8534,     // required, -90 to 90
  "splitterId": 1          // required
}
```

**Response (201 Created):** `BoiteClientDto`

### 10.4 Update Boite Client
**PUT** `/{id}`

**Role:** ADMIN

**Response (200 OK):** `BoiteClientDto`

### 10.5 Connect Port
**PATCH** `/{id}/connect`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):** `BoiteClientDto`

### 10.6 Delete Boite Client
**DELETE** `/{id}`

**Role:** ADMIN

**Response:** 204 No Content

---

## 11. Equipement API
**Base Path:** `/api/v1/network/equipements`

### 11.1 Get All Equipements
**GET** `/`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "nom": "string",
    "modele": "string",
    "numSerie": "string",
    "ip": "192.168.1.1",
    "status": "ACTIF",
    "type": "OLT",
    "repartiteurId": 1,
    "repartiteurNom": "string"
  }
]
```

### 11.2 Get Equipement by ID
**GET** `/{id}`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):** `EquipementDto`

### 11.3 Create Equipement
**POST** `/`

**Role:** ADMIN

**Request Body:**
```tson
{
  "nom": "string",         // required, max 100
  "modele": "string",      // required, max 100
  "numSerie": "string",    // required, max 100
  "ip": "192.168.1.1",     // required, valid IPv4
  "status": "ACTIF",       // required
  "type": "OLT",           // required
  "repartiteurId": 1       // required
}
```

**Response (201 Created):** `EquipementDto`

### 11.4 Update Equipement
**PUT** `/{id}`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):** `EquipementDto`

### 11.5 Delete Equipement
**DELETE** `/{id}`

**Role:** ADMIN

**Response:** 204 No Content

---

## 12. Chemin Fibre API
**Base Path:** `/api/v1/network/chemins-fibre`

### 12.1 Get All Chemins Fibre
**GET** `/`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):**
```tson
[
  {
    "id": 1,
    "sourceNodeId": 1,
    "destNodeId": 2,
    "longueur": 5.2,
    "typeFibre": "MONOMODE",
    "statut": "OK"
  }
]
```

### 12.2 Get Chemin Fibre by ID
**GET** `/{id}`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):** `CheminFibreDto`

### 12.3 Create Chemin Fibre
**POST** `/`

**Role:** ADMIN

**Request Body:**
```tson
{
  "sourceNodeId": 1,       // required
  "destNodeId": 2,         // required
  "longueur": 5.2,         // required, min 0.001
  "typeFibre": "MONOMODE", // required
  "statut": "OK"           // required
}
```

**Response (201 Created):** `CheminFibreDto`

### 12.4 Update Chemin Fibre
**PUT** `/{id}`

**Roles:** ADMIN, TECHNICIEN

**Response (200 OK):** `CheminFibreDto`

### 12.5 Delete Chemin Fibre
**DELETE** `/{id}`

**Role:** ADMIN

**Response:** 204 No Content

---

## 13. Notification API
**Base Path:** `/api/v1/notifications`

### 13.1 Get My Notifications
**GET** `/me`

**Query Parameters:**
- `page` (default: 0): number
- `size` (default: 20): number

**Response (200 OK):** Page of `NotificationDto`

```tson
{
  "id": 1,
  "destinataire": "string",
  "sujet": "string",
  "contenu": "string",
  "typeCanal": "IN_APP",
  "statut": "ENVOYE",
  "createdAt": "2024-01-01T00:00:00Z",
  "sentAt": "2024-01-01T00:00:01Z",
  "errorMessage": null
}
```

### 13.2 Get Failed Notifications
**GET** `/failed`

**Role:** ADMIN

**Response (200 OK):** Array of `NotificationDto`

### 13.3 Get Notifications by Recipient
**GET** `/`

**Role:** ADMIN

**Query Parameters:**
- `email` (required): string

**Response (200 OK):** Array of `NotificationDto`

### 13.4 Send Notification
**POST** `/send`

**Role:** ADMIN

**Request Body:**
```tson
{
  "destinataire": "string", // required, valid email
  "sujet": "string",        // required
  "contenu": "string",      // required
  "typeCanal": "EMAIL"      // required, EMAIL/SMS/IN_APP
}
```

**Response (201 Created):** `NotificationDto`

### 13.5 Retry Failed Notifications
**POST** `/retry-failed`

**Role:** ADMIN

**Response (200 OK):**
```tson
{
  "emailsRetried": 10
}
```

---

## Enum Values Reference

### Role
- `ADMIN`
- `COMMERCIAL`
- `TECHNICIEN`
- `SUPPORT`
- `CLIENT`
- `PROSPECT`

### FactureStatus
- `EN_ATTENTE`
- `PAYEE`
- `EN_RETARD`
- `ANNULEE`

### DemandeStatus
- `SOUMISE`
- `EN_ANALYSE`
- `DEVIS_ENVOYE`
- `ACCEPTEE`
- `PLANIFIEE`
- `TERMINE`
- `REJETEE`

### AbonnementStatus
- `ACTIF`
- `SUSPENDU`
- `RESILIE`
- `EXPIRE`

### TypeEngagement
- `SANS_ENGAGEMENT`
- `DOUZE_MOIS`
- `VINGT_QUATRE_MOIS`

### Technologie
- `FTTH`
- `FTTB`

### NotificationChannel
- `EMAIL`
- `SMS`
- `IN_APP`

### NotificationStatus
- `PENDING`
- `ENVOYE`
- `ECHOUE`

# Suite de la Documentation API - Support & Users

## 14. Support API
**Base Path:** `/api/v1/tickets`

### 14.1 Get All Tickets (Staff Only)
**GET** `/`

**Roles:** ADMIN, SUPPORT, TECHNICIEN, COMMERCIAL

**Query Parameters:**
- `clientId` (optional): number
- `agentId` (optional): number
- `statut` (optional): `OUVERT`, `EN_COURS`, `RESOLU`, `FERME`
- `priorite` (optional): `BASSE`, `MOYENNE`, `HAUTE`, `CRITIQUE`
- `keyword` (optional): string (search in titre, description)
- `page` (default: 0): number
- `size` (default: 20): number

**Response:** Page of `TicketDto`

```tson
{
  "id": 1,
  "clientId": 1,
  "clientNom": "string string",
  "clientEmail": "string@email.com",
  "titre": "string",
  "description": "string",
  "statut": "OUVERT",
  "priorite": "MOYENNE",
  "dateCreation": "2024-01-01T00:00:00Z",
  "dateLimiteSla": "2024-01-08T00:00:00Z",
  "dateResolution": null,
  "agentId": 5,
  "agentNom": "string string"
}
```

### 14.2 Get My Tickets (Client Only)
**GET** `/my-tickets`

**Response (200 OK):** Array of `TicketDto`

### 14.3 Get Ticket by ID
**GET** `/{id}`

**Access:** Own ticket (CLIENT) or all (Staff)

**Response (200 OK):** `TicketDto`

### 14.4 Get Ticket Messages
**GET** `/{id}/messages`

**Access:** Own ticket (CLIENT) or all (Staff)

**Response (200 OK):** Array of `MessageTicketDto`

```tson
{
  "id": 1,
  "ticketId": 1,
  "auteurId": 1,
  "auteurNom": "string string",
  "auteurEmail": "string@email.com",
  "message": "string",
  "dateEnvoi": "2024-01-01T10:00:00Z"
}
```

### 14.5 Create Ticket
**POST** `/`

**Access:** Any authenticated user

**Request Body:**
```tson
{
  "titre": "string",              // required, max 150
  "description": "string",        // required
  "priorite": "MOYENNE",          // required: BASSE, MOYENNE, HAUTE, CRITIQUE
  "clientId": 1                   // optional - staff only
}
```

**Response (201 Created):** `TicketDto`

### 14.6 Add Reply to Ticket
**POST** `/{id}/replies`

**Access:** Staff or ticket owner

**Request Body:**
```tson
{
  "message": "string"   // required
}
```

**Response (201 Created):** `MessageTicketDto`

### 14.7 Assign Agent to Ticket
**PUT** `/{id}/assign`

**Roles:** ADMIN, SUPPORT

**Request Body:**
```tson
{
  "agentId": 5   // required
}
```

**Response (200 OK):** `TicketDto`

### 14.8 Resolve Ticket
**PUT** `/{id}/resolve`

**Roles:** ADMIN, SUPPORT, TECHNICIEN

**Response (200 OK):** `TicketDto`

### 14.9 Close Ticket
**PUT** `/{id}/close`

**Roles:** ADMIN, SUPPORT

**Response (200 OK):** `TicketDto`

### 14.10 Trigger SLA Check
**POST** `/admin/check-sla`

**Role:** ADMIN

**Response:** 200 OK

---

## 15. Users API
**Base Path:** `/api/v1/users`

### 15.1 Create User (Staff Account)
**POST** `/`

**Role:** ADMIN

**Request Body:**
```tson
{
  "nom": "string",         // required, max 100
  "prenom": "string",      // required, max 100
  "email": "string",       // required, valid email, max 150
  "password": "string",    // required, min 8
  "role": "CLIENT"         // optional, defaults CLIENT
}
```

**Allowed Roles:** `ADMIN`, `COMMERCIAL`, `TECHNICIEN`, `SUPPORT`, `CLIENT`, `PROSPECT`

**Response (201 Created):**
```tson
{
  "id": 1,
  "nom": "string",
  "prenom": "string",
  "email": "string@email.com",
  "role": "CLIENT",
  "enabled": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "lastLoginAt": null
}
```

### 15.2 Get My Profile
**GET** `/me`

**Access:** Authenticated user

**Response (200 OK):** `UserDto`

### 15.3 Get All Users (Admin Only)
**GET** `/`

**Role:** ADMIN

**Query Parameters:**
- `query` (optional): string (search in nom, prenom, email)
- `role` (optional): `ADMIN`, `COMMERCIAL`, `TECHNICIEN`, `SUPPORT`, `CLIENT`, `PROSPECT`
- `enabled` (optional): boolean
- `page` (default: 0): number
- `size` (default: 20): number

**Response:** Page of `UserDto`

### 15.4 Get User by ID (Admin Only)
**GET** `/{id}`

**Role:** ADMIN

**Response (200 OK):** `UserDto`

### 15.5 Update User
**PUT** `/{id}`

**Access:** Own profile (any role) or all (ADMIN)

**Request Body:**
```tson
{
  "nom": "string",         // required, max 100
  "prenom": "string",      // required, max 100
  "email": "string",       // required, valid email, max 150
  "role": "ADMIN",         // ADMIN only
  "enabled": true          // ADMIN only
}
```

**Response (200 OK):** `UserDto`

### 15.6 Delete User (Soft Delete)
**DELETE** `/{id}`

**Role:** ADMIN

**Response:** 204 No Content

---

## Enum Values Reference (Complete)

### Role
- `ADMIN` - Full system access
- `COMMERCIAL` - Sales management
- `TECHNICIEN` - Network installation and maintenance
- `SUPPORT` - Customer support
- `CLIENT` - End user with subscription
- `PROSPECT` - Potential client

### TicketStatus
- `OUVERT` - New ticket, not yet assigned
- `EN_COURS` - Assigned and being worked on
- `RESOLU` - Solved, awaiting closure
- `FERME` - Closed

### TicketPriority
- `BASSE` - Low priority
- `MOYENNE` - Medium priority
- `HAUTE` - High priority
- `CRITIQUE` - Critical, urgent

### FactureStatus
- `EN_ATTENTE` - Pending payment
- `PAYEE` - Paid
- `EN_RETARD` - Overdue
- `ANNULEE` - Cancelled

### DemandeStatus
- `SOUMISE` - Submitted by prospect
- `EN_ANALYSE` - Under review
- `DEVIS_ENVOYE` - Quote sent
- `ACCEPTEE` - Quote accepted
- `PLANIFIEE` - Installation scheduled
- `TERMINE` - Installation complete
- `REJETEE` - Rejected

### AbonnementStatus
- `ACTIF` - Active subscription
- `SUSPENDU` - Suspended
- `RESILIE` - Terminated
- `EXPIRE` - Expired

### TypeEngagement
- `SANS_ENGAGEMENT` - No commitment
- `DOUZE_MOIS` - 12 months commitment
- `VINGT_QUATRE_MOIS` - 24 months commitment

### Technologie
- `FTTH` - Fiber to the Home
- `FTTB` - Fiber to the Building

### NotificationChannel
- `EMAIL` - Email notification
- `SMS` - SMS notification
- `IN_APP` - In-app notification

### NotificationStatus
- `PENDING` - Queued for sending
- `ENVOYE` - Sent successfully
- `ECHOUE` - Failed to send

---

## API Endpoints Summary by Module

| Module | Base Path | Endpoints Count |
|--------|-----------|-----------------|
| Authentication | `/api/v1/auth` | 4 |
| Offers | `/api/v1/offers` | 8 |
| Subscriptions | `/api/v1/subscriptions` | 10 |
| Billing | `/api/v1/billing` | 9 |
| Requests | `/api/v1/requests` | 8 |
| Network - Status | `/api/v1/network` | 2 |
| Network - Datacenters | `/api/v1/network/datacenters` | 5 |
| Network - Repartiteurs | `/api/v1/network/repartiteurs` | 5 |
| Network - Splitters | `/api/v1/network/splitters` | 5 |
| Network - Boites Client | `/api/v1/network/boites-client` | 6 |
| Network - Equipements | `/api/v1/network/equipements` | 5 |
| Network - Chemins Fibre | `/api/v1/network/chemins-fibre` | 5 |
| Notifications | `/api/v1/notifications` | 5 |
| Support | `/api/v1/tickets` | 10 |
| Users | `/api/v1/users` | 6 |

**Total Endpoints:** 93

---

## HTTP Status Codes Used

| Code | Description |
|------|-------------|
| 200 | OK - Successful request |
| 201 | Created - Resource created successfully |
| 204 | No Content - Successful request, no body returned |
| 400 | Bad Request - Validation error |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error |

---

## Authentication Header

All endpoints except `/auth/register` and `/auth/login` require a JWT token:

```
Authorization: Bearer <access_token>
```

---

## Date Formats

- **LocalDate**: `YYYY-MM-DD` (e.g., `2024-01-01`)
- **Instant (ISO 8601)**: `YYYY-MM-DDTHH:mm:ssZ` (e.g., `2024-01-01T10:00:00Z`)

---

## Validation Messages (French)

All validation messages are in French as per the DTO constraints:

| Field | Message |
|-------|---------|
| Email | "L'email est obligatoire" / "Format d'email invalide" |
| Password | "Le mot de passe est obligatoire" / "Le mot de passe doit contenir au moins 8 caractères" |
| Name | "Le nom est obligatoire" |
| Required | "[Field] est obligatoire" |










## Architecture front end 



fibre-optique-frontend/
├── public/
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── api/
│   │   ├── client.ts
│   │   ├── auth.ts
│   │   ├── offers.ts
│   │   ├── subscriptions.ts
│   │   ├── billing.ts
│   │   ├── requests.ts
│   │   ├── network.ts
│   │   ├── support.ts
│   │   ├── notifications.ts
│   │   └── users.ts
│   ├── assets/
│   │   ├── images/
│   │   ├── fonts/
│   │   └── styles/
│   ├── components/
│   │   ├── common/
│   │   │   ├── Layout/
│   │   │   ├── Navigation/
│   │   │   ├── Footer/
│   │   │   ├── Loading/
│   │   │   ├── ErrorBoundary/
│   │   │   └── ProtectedRoute/
│   │   ├── auth/
│   │   │   ├── Login/
│   │   │   ├── Register/
│   │   │   ├── ForgotPassword/
│   │   │   └── ResetPassword/
│   │   ├── dashboard/
│   │   │   ├── ClientDashboard/
│   │   │   ├── AdminDashboard/
│   │   │   └── StaffDashboard/
│   │   ├── offers/
│   │   │   ├── OfferList/
│   │   │   ├── OfferDetail/
│   │   │   ├── OfferCard/
│   │   │   └── OfferManagement/
│   │   ├── subscriptions/
│   │   │   ├── SubscriptionList/
│   │   │   ├── SubscriptionDetail/
│   │   │   ├── SubscriptionForm/
│   │   │   └── ContractDownload/
│   │   ├── billing/
│   │   │   ├── InvoiceList/
│   │   │   ├── InvoiceDetail/
│   │   │   ├── InvoicePayment/
│   │   │   ├── InvoiceDownload/
│   │   │   └── BillingStats/
│   │   ├── requests/
│   │   │   ├── RequestForm/
│   │   │   ├── RequestList/
│   │   │   ├── RequestDetail/
│   │   │   ├── RequestManagement/
│   │   │   └── EligibilityCheck/
│   │   ├── network/
│   │   │   ├── NetworkStatus/
│   │   │   ├── DatacenterManagement/
│   │   │   ├── RepartiteurManagement/
│   │   │   ├── SplitterManagement/
│   │   │   ├── BoiteClientManagement/
│   │   │   ├── EquipementManagement/
│   │   │   └── CheminFibreManagement/
│   │   ├── support/
│   │   │   ├── TicketList/
│   │   │   ├── TicketDetail/
│   │   │   ├── TicketForm/
│   │   │   ├── TicketManagement/
│   │   │   └── MessageThread/
│   │   ├── notifications/
│   │   │   ├── NotificationList/
│   │   │   ├── NotificationBadge/
│   │   │   └── NotificationManagement/
│   │   └── users/
│   │       ├── UserProfile/
│   │       ├── UserManagement/
│   │       └── UserForm/
│   ├── contexts/
│   │   ├── AuthContext.tsx
│   │   ├── NotificationContext.tsx
│   │   └── ThemeContext.tsx
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useApi.ts
│   │   ├── usePagination.ts
│   │   ├── useFilters.ts
│   │   ├── useNotifications.ts
│   │   └── useWebSocket.ts
│   ├── layouts/
│   │   ├── MainLayout.tsx
│   │   ├── AuthLayout.tsx
│   │   ├── AdminLayout.tsx
│   │   └── ClientLayout.tsx
│   ├── pages/
│   │   ├── Home/
│   │   ├── About/
│   │   ├── Contact/
│   │   ├── Offers/
│   │   ├── Eligibility/
│   │   ├── RequestConnection/
│   │   ├── Client/
│   │   │   ├── Dashboard/
│   │   │   ├── Subscriptions/
│   │   │   ├── Billing/
│   │   │   ├── Support/
│   │   │   └── Profile/
│   │   ├── Admin/
│   │   │   ├── Dashboard/
│   │   │   ├── Users/
│   │   │   ├── Offers/
│   │   │   ├── Requests/
│   │   │   ├── Subscriptions/
│   │   │   ├── Billing/
│   │   │   ├── Network/
│   │   │   ├── Support/
│   │   │   └── Notifications/
│   │   └── Staff/
│   │       ├── Dashboard/
│   │       ├── Requests/
│   │       ├── Support/
│   │       └── Network/
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── offers.service.ts
│   │   ├── subscriptions.service.ts
│   │   ├── billing.service.ts
│   │   ├── requests.service.ts
│   │   ├── network.service.ts
│   │   ├── support.service.ts
│   │   ├── notifications.service.ts
│   │   └── users.service.ts
│   ├── store/
│   │   ├── slices/
│   │   │   ├── authSlice.ts
│   │   │   ├── userSlice.ts
│   │   │   ├── offersSlice.ts
│   │   │   ├── subscriptionsSlice.ts
│   │   │   ├── billingSlice.ts
│   │   │   ├── requestsSlice.ts
│   │   │   ├── networkSlice.ts
│   │   │   ├── supportSlice.ts
│   │   │   └── notificationsSlice.ts
│   │   └── store.ts
│   ├── types/
│   │   ├── auth.types.ts
│   │   ├── offers.types.ts
│   │   ├── subscriptions.types.ts
│   │   ├── billing.types.ts
│   │   ├── requests.types.ts
│   │   ├── network.types.ts
│   │   ├── support.types.ts
│   │   └── users.types.ts
│   ├── utils/
│   │   ├── constants.ts
│   │   ├── helpers.ts
│   │   ├── formatters.ts
│   │   ├── validators.ts
│   │   ├── permissions.ts
│   │   └── errorHandlers.ts
│   ├── App.tsx
│   ├── AppRoutes.tsx
│   └── index.ts
├── .env
├── .eslintrc.ts
├── .prettierrc
├── package.tson
├── tailwind.config.ts
└── vite.config.ts