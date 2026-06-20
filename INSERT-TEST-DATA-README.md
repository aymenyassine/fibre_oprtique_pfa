# 📊 Script d'Insertion de Données de Test - FibreOps

## 🎯 Objectif

Ce script remplit votre base de données MySQL `fibre_optique_db` avec des données réalistes pour tester tous les processus de l'application FibreOps.

## 📍 Localisation

Toutes les coordonnées GPS sont **situées au Maroc** :
- 🏢 **Datacenters** : Casablanca, Rabat, Marrakech, Fès, Tanger
- 🔀 **Répartiteurs** : Distribués dans chaque datacenter
- 📦 **Boîtes Client** : Réparties dans les villes principales
- 👥 **Utilisateurs** : Avec numéros de téléphone marocains (+212)

## 🔐 Mot de Passe

Hash BCrypt utilisé pour tous les utilisateurs de test :
```
$2a$10$Dizx9kWLZRxROlFwQ.eFVuVWaOkpaL02pbRSIXFyXqfJDX4l8G/ZO
```

**Correspond à** : `test123`

## 📋 Données Insérées

### Utilisateurs (7 comptes)
| Email | Rôle | Ville |
|-------|------|-------|
| admin@example.com | ADMIN | Casablanca |
| tech1@example.com | TECHNICIEN | Marrakech |
| tech2@example.com | TECHNICIEN | Fès |
| commercial1@example.com | COMMERCIAL | Rabat |
| commercial2@example.com | COMMERCIAL | Tanger |
| client1@example.com | CLIENT | Agadir |
| client2@example.com | CLIENT | Meknès |

### Infrastructure Réseau
- ✅ **5 Datacenters** stratégiquement placés au Maroc
- ✅ **10 Répartiteurs** (96 à 48 ports)
- ✅ **15 Splitters** (ratios 1:32, 1:16)
- ✅ **14 Boîtes Client** avec ports utilisés
- ✅ **10 Équipements** (OLT, SWITCH, ROUTEUR, AMPLI)
- ✅ **25+ Chemins Fibre** (liaisons DC, Répartiteur, Boîte)

### Services
- ✅ **5 Offres** (Fibre 100 à 1000 Mbps, FTTB)
- ✅ **4 Abonnements** actifs/en attente
- ✅ **3 Contrats** signés
- ✅ **6 Factures** (payées, en attente, en retard)
- ✅ **2 Demandes Raccordement**
- ✅ **2 Interventions** (planifiée, complétée)
- ✅ **2 Tickets Support**
- ✅ **3 Notifications**

### État du Réseau
- 🟢 Statuts : OK, MAINTENANCE, INCIDENT
- 📊 Couverture réaliste du Maroc
- 🔗 Liaisons inter-datacenters longue distance

## 🚀 Utilisation

### Option 1 : Script PowerShell (Recommandé - Windows)

```powershell
cd back
.\execute-sql.ps1
# Entrez le mot de passe MySQL root quand demandé
```

**Avantages** :
- Demande interactive du mot de passe
- Feedback en temps réel
- Gestion d'erreurs améliorée
- Affichage des credentials après succès

### Option 2 : Script Batch (Windows)

```batch
cd back
# Editez execute-sql.bat et remplacez "password" par votre mot de passe MySQL
execute-sql.bat
```

### Option 3 : Ligne de Commande MySQL (Linux/Mac/Windows)

```bash
# Sans mot de passe (si configuré)
mysql -u root -h localhost fibre_optique_db < insert-test-data.sql

# Avec mot de passe
mysql -u root -p -h localhost fibre_optique_db < insert-test-data.sql
# Puis entrez le mot de passe quand demandé
```

### Option 4 : MySQL Workbench ou DBeaver

1. Ouvrez votre client MySQL
2. Connectez-vous à `fibre_optique_db`
3. Ouvrez `insert-test-data.sql`
4. Exécutez le script

## ✅ Vérification

Une fois exécuté, le script affiche un résumé :

```
===== RÉSUMÉ DES DONNÉES DE TEST =====
Utilisateurs: 7
Datacenters: 5
Répartiteurs: 10
Splitters: 15
Boîtes Clients: 14
Équipements: 10
Chemins Fibre: 25
Offres: 5
Abonnements: 4
Contrats: 3
Factures: 6
Demandes Raccordement: 2
Interventions: 2
Tickets Support: 2
Notifications: 3
=====================================
```

## 🧪 Cas de Test Disponibles

### Authentification
```
Email: admin@example.com
Motdepasse: test123
```

### Gestion Réseau
- ✅ Lister les datacenters (5 au Maroc)
- ✅ Créer/modifier des répartiteurs
- ✅ Gérer les boîtes client
- ✅ Visualiser chemins fibre
- ✅ Voir les incidents en réseau

### Gestion Abonnements
- ✅ Voir les abonnements actifs
- ✅ Consulter les contrats
- ✅ Afficher les factures (payées, en attente, en retard)

### Support Client
- ✅ Tickets support avec statuts différents
- ✅ Interventions planifiées et complétées
- ✅ Notifications

### Traçage de Chemin
- ✅ Sélectionner une boîte client
- ✅ Visualiser le chemin : Datacenter → Répartiteur → Splitter → Boîte → Domicile
- ✅ Voir les liaisons fibre sur la carte Google Maps

## 🗺️ Localités Maroc Incluses

**Datacenters** :
- 🏢 Casablanca (Latitude: 33.5731, Longitude: -7.5898)
- 🏢 Rabat (Latitude: 34.0209, Longitude: -6.8416)
- 🏢 Marrakech (Latitude: 31.6295, Longitude: -8.0029)
- 🏢 Fès (Latitude: 34.0333, Longitude: -5.0167)
- 🏢 Tanger (Latitude: 35.7595, Longitude: -5.8080)

**Boîtes Client** dans :
- 📍 Casablanca (6 boîtes)
- 📍 Rabat (3 boîtes)
- 📍 Marrakech (3 boîtes)
- 📍 Fès (1 boîte)
- 📍 Tanger (1 boîte)

## 🔧 Configuration MySQL

### Prérequis
- MySQL 8.0+ (ou 5.7+)
- Base de données `fibre_optique_db` créée
- Utilisateur avec permissions (généralement `root`)

### Exécution Autorisée
Le script ne supprime/remplace que les tables de données de test.
**Ne supprime pas** :
- Les tables de structure (users, offers, etc. si déjà populeés)
- Les données de production

### Réinitialisation
Pour réinitialiser complètement, réexécutez simplement le script.
Les sections `DELETE FROM` nettoient les anciennes données avant insertion.

## 📞 Numéros de Test

Tous les numéros de téléphone utilisent le format marocain :
```
+212 6XX XXXXXX (mobile Maroc)
+212 5XX XXXXXX (fixe Maroc)
```

Exemples utilisés :
- +212612345678 (Maroc Télécom)
- +212698765432 (Orange Maroc)
- +212611223344 (Maroc Télécom)

## ⚠️ Important

- **Ne modifiez PAS les IDs** dans les INSERT (ils sont auto-générés)
- **Utilisez toujours le même hash** pour la cohérence
- **Testez d'abord sur une DB de dev** avant production
- **Sauvegardez votre DB** avant d'exécuter si elle contient des données

## 🎯 Après l'Exécution

Vous pouvez maintenant :
1. ✅ Vous connecter avec `admin@example.com` / `test123`
2. ✅ Explorer tous les modules (Réseau, Facturation, Support)
3. ✅ Tester les chemins fibre en traçage
4. ✅ Créer des abonnements
5. ✅ Gérer les tickets support
6. ✅ Vérifier les rapports et statistiques

## 📞 Support

Si vous rencontrez des problèmes :
1. Vérifiez que MySQL est en cours d'exécution
2. Vérifiez vos credentials MySQL
3. Assurez-vous que `fibre_optique_db` existe
4. Consultez les logs MySQL pour plus de détails

---

**Script généré le** : 20 juin 2026  
**Compatible avec** : FibreOps v1.0+  
**Dernière mise à jour** : 20 juin 2026
