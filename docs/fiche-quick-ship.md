# Fiche Projet : Quick-Ship

> Source : énoncé professeur (catalogue Fiche 16). Ne rien inventer au-delà de ce document + jalons Classroom.

## Description

Transport de colis entre entrepôts : un colis est accepté sur une tournée, suivi à chaque étape, livré avec preuve ; les colis perdus ou retournés déclenchent automatiquement une réclamation client.

## Unité de traitement

**Le Colis** — cycle de vie :

`EN_ATTENTE` → `EN_TRANSIT` → `LIVRE` / `RETOUR` / `PERDU`

## Règle métier clé

> Un Colis = 1 expéditeur + 1 destinataire + 1 tournée + 1 numéro de suivi

---

## Services & Entités

### service-reseau

Dépôts logistiques et lignes de tournée.

| Entité | Responsabilité unique | Attributs |
|--------|----------------------|-----------|
| Depot | Un dépôt logistique | id, nom, ville, capacite |
| Route | Une tournée de transport datée | id, nom, villeDepart, villeArrivee, dateDepart, dureeEstimee, capaciteColis |

### service-expeditions ⭐ (service principal)

Expéditions et jalons de suivi.

| Entité | Responsabilité unique | Attributs |
|--------|----------------------|-----------|
| Shipment | Un colis expédié | id, customerId, destinataireNom, adresseDestinataire, poids, routeId, montantTransport, numeroSuivi, statut |
| TrackingEntry | Un jalon de suivi | id, shipmentId, date, localisation, commentaire |

### service-clients

Comptes expéditeurs et réclamations.

| Entité | Responsabilité unique | Attributs |
|--------|----------------------|-----------|
| Customer | Un compte expéditeur | id, nom, email, encours, plafondCredit, statut |
| Claim | Une réclamation client | id, customerId, shipmentId, description, montant, statut |

### Autres services (selon jalons)

| Service | Rôle |
|---------|------|
| service-notification | Notifier l'expéditeur (RabbitMQ en V5, Kafka en V7) |
| Config Server | Centralisation de config (Jalon Classroom v3.0) |
| API Gateway | Rate limiting (référencé en V6 / V7) |

---

## Jalons de réalisation (énoncé fiche)

### V1 : CRUD fonctionnel

Implémenter le CRUD des entités :

- `Depot`, `Route` → **service-reseau**
- `Customer` → **service-clients**

### V4 : CRUD avec validation externe et consultation enrichie

Implémenter le CRUD et les opérations métier de :

- `Shipment`, `TrackingEntry`, `Claim`

Règles OpenFeign :

1. Vérifier le compte expéditeur et son encours auprès de **service-clients**
2. Vérifier la destination et la capacité de la tournée datée auprès de **service-reseau**
3. Réserver la capacité avant le départ
4. Vérifier le colis d'une réclamation auprès de **service-expeditions**
5. Calculer le montant de transport côté serveur

Endpoint enrichi :

- Consultation d'un colis avec : expéditeur + tournée + suivi + réclamations

### V5 : Notifications asynchrones avec RabbitMQ

- Ajouter **service-notification** qui consomme RabbitMQ
- Notifier l'expéditeur lors de la **livraison** ou du **retour** de son colis
- Publier les événements après validation de l'opération métier

### V7 : Transactions métier en architecture événementielle avec Kafka

Créer une branche à partir de **V3** et réimplémenter les traitements multi-services avec Kafka :

| Événement | Flux | Action |
|-----------|------|--------|
| `shipment-requested` | service-expeditions → service-reseau | Réserver atomiquement 1 unité de capacité sur la tournée |
| `shipment-capacity-reserved` / `shipment-capacity-rejected` | service-reseau → service-expeditions | Autoriser le départ ou garder le colis en attente |
| `shipment-in-transit` | service-expeditions → service-reseau | Valider l'affectation **sans** second décompte de capacité |
| `shipment-delivered` | service-expeditions (après LIVRE) → service-reseau | Libérer la capacité |
| | → service-clients | Augmenter l'encours du montant du transport |
| | → service-notification | Notifier l'expéditeur |
| `parcel-returned` / `parcel-lost` | service-expeditions → service-reseau | Libérer la capacité |
| | → service-clients | Ouvrir **une seule** Claim |
| | → service-notification | Notifier l'expéditeur |

Contraintes V7 :

- Reprendre **service-notification** de V5 en remplaçant RabbitMQ par Kafka
- Reprendre le Rate Limiting de la Gateway configuré en V6
- Les services métier publient ; seul **service-notification** envoie aux destinataires
- Réserver la capacité avant départ ; la libérer **une seule fois** à la fin
- Dédupliquer montants portés à l'encours et réclamations retour/perte
- Assurer transactions locales + idempotence des consommateurs

---

## Jalons Classroom (J2EE Avancée 2027) — à croiser avec la fiche

| Jalon Classroom | Contenu |
|-----------------|---------|
| Jalon Init | Conception de l'application sur README.md |
| Jalon v1.0 | Mise en place des services métier |
| Jalon v3.0 | Mise en place du Config Server |
| Jalon v4.0 | Communication synchrone avec OpenFeign |
| Jalon v5.0 | Communication asynchrone et notifications avec RabbitMQ |
| Jalon v6.0 | Résilience, gestion des erreurs et idempotence |

> Note : la fiche parle aussi de V7 (Kafka). Le Classroom liste explicitement Config Server (v3) et Résilience (v6). Suivre les deux sources sans inventer le détail manquant.

---

## Sujet choisi

**Fiche 16 — Quick-Ship** (logistique de colis)
