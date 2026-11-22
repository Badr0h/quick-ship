# Quick-Ship

Projet **J2EE Avancée 2027** (5IDSI) — Fiche 16.

Transport de colis entre entrepôts : un colis est accepté sur une tournée, suivi à chaque étape, livré avec preuve ; les colis perdus ou retournés déclenchent automatiquement une réclamation client.

## Unité de traitement

**Le Colis** — cycle de vie :

```
EN_ATTENTE → EN_TRANSIT → LIVRE / RETOUR / PERDU
```

> Un Colis = 1 expéditeur + 1 destinataire + 1 tournée + 1 numéro de suivi

## Architecture des services

| Service | Rôle | Entités |
|---------|------|---------|
| **service-reseau** | Dépôts logistiques et lignes de tournée | `Depot`, `Route` |
| **service-expeditions** ⭐ | Expéditions et jalons de suivi (service principal) | `Shipment`, `TrackingEntry` |
| **service-clients** | Comptes expéditeurs et réclamations | `Customer`, `Claim` |
| **service-notification** | Notifications à l'expéditeur | — |
| **Config Server** | Configuration centralisée | — |
| **API Gateway** | Entrée unique (+ rate limiting) | — |

### Attributs des entités

#### Depot
`id`, `nom`, `ville`, `capacite`

#### Route
`id`, `nom`, `villeDepart`, `villeArrivee`, `dateDepart`, `dureeEstimee`, `capaciteColis`

#### Shipment
`id`, `customerId`, `destinataireNom`, `adresseDestinataire`, `poids`, `routeId`, `montantTransport`, `numeroSuivi`, `statut`

#### TrackingEntry
`id`, `shipmentId`, `date`, `localisation`, `commentaire`

#### Customer
`id`, `nom`, `email`, `encours`, `plafondCredit`, `statut`

#### Claim
`id`, `customerId`, `shipmentId`, `description`, `montant`, `statut`

## Jalons de réalisation

| Jalon | Contenu |
|-------|---------|
| **Init** | Conception de l'application (ce README) |
| **V1** | CRUD : `Depot`, `Route` (service-reseau) ; `Customer` (service-clients) |
| **V3** | Config Server |
| **V4** | CRUD `Shipment`, `TrackingEntry`, `Claim` + validations OpenFeign + consultation enrichie |
| **V5** | Notifications asynchrones (RabbitMQ) via service-notification |
| **V6** | Résilience, gestion des erreurs et idempotence (+ rate limiting Gateway) |
| **V7** | Transactions événementielles avec Kafka (branche depuis V3) |

### V4 — Validations OpenFeign

1. Vérifier le compte expéditeur et son encours (`service-clients`)
2. Vérifier destination et capacité de la tournée datée (`service-reseau`)
3. Réserver la capacité avant le départ
4. Vérifier le colis d'une réclamation (`service-expeditions`)
5. Calculer le montant de transport côté serveur
6. Endpoint de consultation enrichie : expéditeur + tournée + suivi + réclamations

### V5 — RabbitMQ

Notifier l'expéditeur lors de la **livraison** ou du **retour** ; publier les événements après validation métier.

### V7 — Kafka (événements)

| Événement | Flux | Action |
|-----------|------|--------|
| `shipment-requested` | expeditions → reseau | Réserver 1 unité de capacité |
| `shipment-capacity-reserved` / `rejected` | reseau → expeditions | Autoriser le départ ou rester en attente |
| `shipment-in-transit` | expeditions → reseau | Valider l'affectation sans second décompte |
| `shipment-delivered` | expeditions → reseau / clients / notification | Libérer capacité, augmenter encours, notifier |
| `parcel-returned` / `parcel-lost` | expeditions → reseau / clients / notification | Libérer capacité, ouvrir une seule Claim, notifier |

## Documentation

Spécification détaillée : [`docs/fiche-quick-ship.md`](docs/fiche-quick-ship.md)
