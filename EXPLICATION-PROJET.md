# Explication complète — Projet Quick-Ship

Ce document sert à **comprendre** et **expliquer au professeur** tout le projet.  
Repo : https://github.com/Badr0h/quick-ship  
Sujet : **Fiche 16 — Quick-Ship** (logistique de colis)  
Cours : J2EE Avancée 2027 (5IDSI)

---

## 1. C’est quoi Quick-Ship ?

Application de **transport de colis entre entrepôts** :

1. Un **expéditeur** (Customer) crée un **colis** (Shipment)
2. Le colis est placé sur une **tournée** (Route)
3. On suit le colis étape par étape (TrackingEntry)
4. À la fin : **livré**, **retourné** ou **perdu**
5. Si retour / perdu → **réclamation** (Claim) automatique
6. L’expéditeur reçoit un **e-mail** de notification

### Cycle de vie du colis

```
EN_ATTENTE → EN_TRANSIT → LIVRE
                       → RETOUR
                       → PERDU
```

**Règle métier :**  
Un Colis = 1 expéditeur + 1 destinataire + 1 tournée + 1 numéro de suivi

---

## 2. Architecture (microservices)

| Service | Port | Rôle | Entités |
|---------|------|------|---------|
| **service-clients** | 8081 | Comptes expéditeurs + réclamations | Customer, Claim |
| **service-reseau** | 8082 | Dépôts + tournées + capacité | Depot, Route |
| **service-expeditions** ⭐ | 8083 | Colis + suivi (service principal) | Shipment, TrackingEntry |
| **service-notification** | 8084 | Envoi des e-mails | — |
| **service-registry** | 8761 | Eureka (découverte des services) | — |
| **gateway** | 8080 | Entrée unique API | — |
| **config-server** | 8888 | Config centralisée (`config-repo/`) | — |

### Infrastructure Docker

| Outil | Usage | URL |
|-------|--------|-----|
| RabbitMQ | Messages async (V5/V6 sur `main`) | http://localhost:15672 |
| Kafka + Kafka UI | Messages async (V7) | http://localhost:8090 |
| Mailpit | Boîte mail de test | http://localhost:8025 |

### Schéma simple

```
Postman / Client
       │
       ▼
   Gateway :8080
       │
       ├── service-clients
       ├── service-reseau
       ├── service-expeditions ──OpenFeign──► clients + reseau
       │            │
       │            └── RabbitMQ (main) ou Kafka (v7.0) ──► notification ──► Mailpit
       │
Eureka ◄── tous les services s’enregistrent
Config Server ◄── tous chargent leur config
```

---

## 3. Les jalons (ce que le prof attend)

### Jalon Init — Conception
- README avec architecture, entités, cycle de vie
- Fiche projet dans `docs/fiche-quick-ship.md`

### V1 — CRUD métier
- `Depot`, `Route` dans **service-reseau**
- `Customer` dans **service-clients**
- Architecture couches : Entity → Repository → Service → Controller
- Une base H2 **par service** (Database per Service)

### V2 — Eureka + Gateway
- Eureka sur 8761
- Services enregistrés
- Gateway route dynamiquement via le nom du service  
  Exemple : `http://localhost:8080/service-clients/customers`

### V3 — Config Server
- Config externalisée dans `config-repo/`
- Config Server sur 8888 (profil native)

### V4 — OpenFeign + consultation enrichie
À la création d’un colis, **service-expeditions** appelle :
1. **service-clients** → vérifie le compte + encours / plafond
2. **service-reseau** → vérifie la tournée + capacité + destination

Puis :
- calcule `montantTransport = poids * 10 + 50`
- réserve 1 place sur la tournée (sync Feign en V4–V6 ; Kafka en V7)

**Endpoint enrichi :**  
`GET /shipments/{id}/full`  
→ colis + expéditeur + tournée + suivi + réclamations

### V5 — RabbitMQ + notifications
- Après **livraison** ou **retour**, publication d’un message
- **service-notification** consomme et envoie un mail (Mailpit)

### V6 — Résilience + erreurs + idempotence
- **Circuit Breaker + Retry + timeout** sur les appels Feign (clients + réseau)
- **ControllerAdvice** (erreurs centralisées)
- **Idempotence** : header `idempotencyKey` à la création du colis  
  (même clé = pas de double création)
- **RateLimiter** sur la consultation enrichie `/full`

### V7 — Kafka (branche `v7.0`)
RabbitMQ est remplacé par Kafka. Flux événementiel :

| Événement | Qui → Qui | Action |
|-----------|-----------|--------|
| `shipment.requested` | expeditions → reseau | Réserver capacité |
| `shipment.capacity.reserved` / `rejected` | reseau → expeditions | Autoriser ou non le départ |
| `shipment.in-transit` | expeditions → reseau | Valider sans 2e décompte |
| `shipment.delivered` | expeditions → reseau + clients + notif | Libérer capacité, ↑ encours, mail |
| `parcel.returned` / `parcel.lost` | expeditions → reseau + clients + notif | Libérer, 1 Claim, mail |

**Points importants V7 :**
- Capacité réservée **atomiquement** (verrou)
- Capacité libérée **une seule fois**
- Consommateurs **idempotents** (`processed_event`)
- Seul **service-notification** envoie les mails

---

## 4. Comment démarrer (ordre)

```bash
# 1. Infra
docker compose up -d

# 2. Apps (dans IntelliJ ou mvn spring-boot:run)
config-server          # 8888
service-registry       # 8761
gateway                # 8080
service-clients        # 8081
service-reseau         # 8082
service-expeditions    # 8083
service-notification   # 8084
```

- **Pour RabbitMQ (V5/V6)** : branche `main` / tag `v6.0`
- **Pour Kafka (V7)** : `git checkout v7.0`

Tests : importer `postman/Quick-Ship.postman_collection.json`

---

## 5. Scénario de démo (à raconter au prof)

1. Lister les customers / routes (CRUD V1)
2. Créer un colis avec header `idempotencyKey` (V4 + V6)
3. Montrer Eureka (services UP) et Gateway
4. Consultation `/shipments/{id}/full` (agrégation Feign)
5. Départ → Transit → Livraison
6. Vérifier le mail dans Mailpit (V5 ou V7)
7. (Option) Arrêter un service → Circuit Breaker (V6)
8. (V7) Montrer les messages dans Kafka UI

---

## 6. Où est quoi dans le repo

| Élément | Chemin |
|---------|--------|
| **PDF unique V0→V7** | [`docs/QUICK-SHIP-V0-V7.pdf`](docs/QUICK-SHIP-V0-V7.pdf) |
| Guide dépôt Classroom | `DEPOSER-CLASSROOM.md` |
| Conception / archi | `README.md` |
| Fiche sujet | `docs/fiche-quick-ship.md` |
| PDFs des TPs | `docs/jalon v2.0` … `docs/jalon v7.0` |
| Postman | `postman/Quick-Ship.postman_collection.json` |
| Config centralisée | `config-repo/` |
| Docker | `docker-compose.yml` |

---

## 7. Branches & tags

| Ref | Contenu |
|-----|---------|
| `main` / `v6.0` | Projet jusqu’à V6 (RabbitMQ) |
| `v1.0` … `v5.0` | Tags Classroom (socle consolidé) |
| `v7.0` / `v7.0-release` | Version Kafka |

---

## 8. Phrase résumé (30 secondes)

> « Quick-Ship est une appli microservices Spring Cloud pour le suivi de colis.  
> Trois services métier (clients, réseau, expéditions), plus Eureka, Gateway, Config Server et un service de notification.  
> La communication synchrone passe par OpenFeign, l’asynchrone par RabbitMQ (V5) puis Kafka (V7).  
> On a ajouté résilience Resilience4j, gestion d’erreurs centralisée et idempotence. »

---

## 9. Collaborateurs

- https://github.com/Badr0h (owner)
- https://github.com/aymanehaska23-coder (write — accepter l’invitation)
- https://github.com/Salaheddine-Merchich (write — accepter l’invitation)
