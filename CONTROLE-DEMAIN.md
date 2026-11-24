# Quick-Ship — Ce que tu dois savoir demain (30 min)

Lis dans l’ordre. Chaque phrase = 1 idée. Pas besoin du PDF.

Branche actuelle du repo : `v7.0` **= Kafka**.  
La grille 1ère éval note surtout **V0→V6** (sur `main` = RabbitMQ).  
Tu dois savoir **les deux idées** : broker + mail (V5) et Kafka event-driven (V7).

---

- 0. C’est quoi ce projet ?
- **Quick-Ship** = backend de suivi de colis entre entrepôts.
- Un **client** (expéditeur) crée un **colis**
- Le colis est mis sur une **tournée**
- On suit le colis

1. Fin : **livré** / **retourné** / **perdu**
2. Si retour/perdu → **réclamation** + **mail**

Cycle :

```
EN_ATTENTE → EN_TRANSIT → LIVRE
                       → RETOUR
                       → PERDU
```

Formule : `montantTransport = poids × 10 + 50` (calculé côté serveur).

**C’est uniquement un backend** (API REST). Pas de frontend. On teste avec **Postman**.

---

## 1. Microservice — le concept de base

### Monolithe vs microservice

| Monolithe | Microservices |
|-----------|---------------|
| 1 grosse app | Plusieurs petites apps |
| 1 base souvent | 1 base **par** service |
| Tout casse ensemble | Un service peut planter sans tout tuer |
| 1 déploiement | Déploiements séparés |

**Définition à dire :**  
Un microservice = une app avec **une responsabilité**, sa **propre base**, déployable **toute seule**, qui parle aux autres via **API** ou **messages**.

Chez nous :

| Service | Responsabilité |
|---------|----------------|
| `service-clients` | Clients + réclamations |
| `service-reseau` | Dépôts + tournées + capacité |
| `service-expeditions` | Colis + suivi (le cœur) |
| `service-notification` | Envoi des mails **seulement** |

---

## 2. Architecture du projet (où est quoi)

```
Postman
   │
   ▼
Gateway :8080          ← entrée unique
   │
   ├── service-clients :8081
   ├── service-reseau :8082
   ├── service-expeditions :8083  ──Feign──► clients + réseau
   │              │
   │              └── Kafka (v7) / RabbitMQ (main) ──► notification :8084 ──► Mailpit
   │
Eureka :8761           ← annuaire (qui est UP ?)
Config Server :8888    ← configs dans config-repo/
```

### Ports à connaître par cœur

| Chose | Port | Dossier |
|-------|------|---------|
| Gateway | 8080 | `gateway/` |
| Eureka | 8761 | `service-registry/` |
| Config Server | 8888 | `config-server/` |
| Clients | 8081 | `service-clients/` |
| Réseau | 8082 | `service-reseau/` |
| Expéditions | 8083 | `service-expeditions/` |
| Notification | 8084 | `service-notification/` |
| Mailpit (mails test) | 8025 | Docker |
| Kafka UI | 8090 | Docker |
| Kafka broker | 9092 | Docker |

### Bases de données

Chaque service métier a **sa propre H2 en mémoire** :

- `clientsdb`
- `reseaudb`
- `expeditionsdb`
- `notificationdb` (pour `processed_event`)

→ C’est le pattern **Database per Service**.  
On ne joint **pas** les tables entre services. On stocke juste un **ID** (`customerId`, `routeId`…) = **référence distante**.

---

## 3. Les “pièces Spring Cloud” (V2–V3)

### Eureka (Service Discovery)

**Problème :** comment savoir où tourne `service-clients` ? Port ? IP ?

- **Solution :** Eureka = **annuaire**.  
Chaque service s’enregistre : « je suis `service-clients`, je suis UP ».  
Les autres le trouvent **par nom**, pas par IP fixe.
- Code : `service-registry/`
- UI : http://localhost:8761
- Config typique : `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`

**Phrase :** Eureka = l’annuaire des services UP.

### Gateway (API Gateway)

**Problème :** le client ne doit pas connaître 4 ports.

**Solution :** une seule porte d’entrée `:8080`.  
Exemple : `http://localhost:8080/service-clients/customers`  
→ Gateway demande à Eureka où est `service-clients`, puis forward.

**Phrase :** Gateway = porte d’entrée unique + routage (+ load balancing possible).

### Config Server

**Problème :** mettre les configs dans chaque jar = galère.

**Solution :** configs centralisées dans `config-repo/`.  
Au démarrage, chaque service tire sa config depuis Config Server `:8888`.

Fichiers : `config-repo/service-clients.properties`, etc.

**Phrase :** Config Server = une source de config pour tous les services.

### Ordre de démarrage (important)

1. `docker compose up -d` (infra)
2. `config-server`
3. `service-registry` (Eureka)
4. `gateway`
5. `service-clients` / `service-reseau` / `service-expeditions` / `service-notification`

---

## 4. Couches dans un service (V1)

Dans chaque microservice Spring Boot, même organisation :

```
Controller  → reçoit HTTP, appelle le Service
Service     → métier (règles)
Repository  → accès DB (JPA)
Entity      → table
DTO         → ce qu’on envoie/reçoit en JSON (+ validation)
```

**Pourquoi DTO ?** On n’expose pas l’Entity brute. Validation (`@NotNull`, etc.) sur l’entrée.

**CRUD autonome :** chaque service gère ses données sans dépendre des autres *pour son CRUD simple* (V1).  
Les dépendances viennent plus tard (Feign / Kafka).

Postman : `postman/Quick-Ship.postman_collection.json`

---

## 5. OpenFeign — communication SYNCHRONE (V4)

### Sync vs async (à maîtriser)

| Sync (Feign) | Async (broker) |
|--------------|----------------|
| J’appelle et **j’attends** la réponse | Je publie un message et **je continue** |
| Pour **décider maintenant** | Pour **effets différés** (mail, capacité…) |
| Si l’autre est down → je le sais tout de suite | Si l’autre est down → le message attend |

### C’est quoi Feign ?

OpenFeign = client HTTP **typé**.  
Au lieu d’écrire du `RestTemplate` moche, tu déclares une interface :

```java
@FeignClient(name = "service-clients")
public interface ClientsRequest {
    @GetMapping("/customers/{id}")
    CustomerDto getCustomerById(@PathVariable Long id);
}
```

Feign + Eureka : `name = "service-clients"` → trouve l’instance UP automatiquement.

Chez nous : `service-expeditions/.../feign/ClientsRequest.java` et `ReseauRequest.java`

### Quand on l’utilise (création colis)

Avant d’accepter un colis, expeditions doit savoir **tout de suite** :

1. Client existe + statut ACTIF + encours/plafond OK → Feign vers **clients**
2. Tournée existe + destination OK + place dispo → Feign vers **réseau**
3. Calcule le montant
4. Sauve le colis `EN_ATTENTE`
5. (V7) Publie un event Kafka pour réserver la capacité

### Consultation enrichie `/full`

`GET /service-expeditions/shipments/{id}/full`

Un seul JSON composé :

- shipment (local)
- + customer (Feign clients)
- + route (Feign réseau)
- + tracking (local)
- + claims (Feign clients)

**Phrase :** Feign = HTTP sync typé quand j’ai besoin de la réponse pour décider.

---

## 6. Broker / messaging — communication ASYNCHRONE (V5–V7)

### C’est quoi un broker ?

Un **intermédiaire de messages**.

```
Service A  ──publie──►  BROKER  ──délivre──►  Service B
```

A et B ne se parlent **pas directement**.  
Avantage : si B est down, le message attend. A a déjà fini son boulot.

**Publisher** = celui qui envoie le message.  
**Consumer** = celui qui le reçoit et agit.

### Pourquoi un broker pour le mail ?

Quand un colis est livré :

1. On commit en DB : statut = LIVRE
2. On publie un message « envoie un mail »
3. `service-notification` consomme → envoie le mail

Si la notif est down : **la livraison reste valide**. Le mail partira plus tard.  
= **découplage**.

### afterCommit (important)

On publie **après** le commit DB.  
Sinon : message envoyé, mais rollback DB → event orphelin (mail pour un colis qui n’existe pas).

---

## 7. RabbitMQ vs Kafka

### RabbitMQ (branche `main`, jalons V5/V6)

- File de messages classique
- Un message → en général consommé puis retiré
- Très bien pour : « envoie ce mail »
- UI souvent : http://localhost:15672

**Rôle dans Quick-Ship V5 :** après LIVRE/RETOUR/PERDU → message → notification → mail.

### Kafka (branche `v7.0`, jalon V7)

- **Log d’événements** (pas juste une file)
- Les messages sont **gardés** un moment (rétention) → on peut **rejouer**
- **Plusieurs consommateurs** indépendants sur le **même** event  
Exemple : un `shipment.delivered` est lu par **réseau** + **clients** + (via notif) le mail

**Phrase magique :**  
Rabbit = file pour faire une action (mail).  
Kafka = journal d’events métier + multi-consommateurs + rejeu.

### Feign disparaît en V7 ?

**Non.**  

- Feign = lire / décider (client OK ? route OK ? `/full`)  
- Kafka = effets async (réserver capacité, libérer, ↑ encours, claim, mail)

---

## 8. Kafka dans NOTRE projet (v7) — flux concret

Fichiers clés :

| Rôle | Fichier |
|------|---------|
| Publier | `service-expeditions/.../ShipmentEventPublisher.java` |
| Topics | `.../KafkaTopics.java` |
| Conso capacité | `service-reseau/.../ReseauKafkaConsumer.java` |
| Conso encours/claim | `service-clients/.../ClientsKafkaConsumer.java` |
| Conso mail | `service-notification/.../NotificationConsumer.java` |
| Résultat capacité | `.../CapacityResultConsumer.java` |

### Le film d’un colis (à raconter oralement)

1. **POST create**
   - Feign vérifie client + route  
   - Sauve `EN_ATTENTE`  
   - Publie `shipment.requested`
2. **service-reseau** reçoit → réserve 1 place (atomique)
   - OK → `shipment.capacity.reserved`  
   - KO → `shipment.capacity.rejected`
3. **depart** (seulement si capacité réservée)
   - Statut → `EN_TRANSIT`  
   - Publie `shipment.in-transit` (**sans** re-décompter)
4. **livrer**
   - Statut → `LIVRE`  
   - Publie `shipment.delivered`  
   - réseau libère capacité  
   - clients ↑ encours  
   - notification → mail
5. **retour / perdu**
   - Libère capacité  
   - Crée **1 Claim**  
   - Mail

### Contraintes à retenir

- Réserver **avant** départ
- Libérer **une seule fois**
- **Une seule Claim** par colis
- Consommateurs **idempotents** (`processed_event`)
- Seul **notification** envoie les mails

Diagramme : `docs/diagrams/v7-kafka.png`

---

## 9. Docker Compose — qu’est-ce qui se passe ?

Fichier : `docker-compose.yml`

**Important :** Docker ne lance **PAS** tes microservices Spring.  
Il lance seulement l’**infrastructure** :

| Conteneur | À quoi ça sert |
|-----------|----------------|
| `kafka` | Broker de messages (:9092) |
| `kafka-ui` | Voir les topics (:8090) |
| `mailpit` | Fausse boîte mail (:8025) + SMTP (:1025) |

Quand tu fais :

```bash
docker compose up -d
```

→ Kafka + Kafka UI + Mailpit démarrent.  
Ensuite **toi** tu lances les apps Spring (IntelliJ / `mvn spring-boot:run`).

Sur `main` (V5/V6), l’infra broker serait plutôt **RabbitMQ** (selon le compose de cette branche).

Mailpit = pas un vrai Gmail. C’est une boîte locale pour **prouver** que le mail est parti.

---

## 10. Résilience & idempotence (V6)

Fichier clé : `service-expeditions/.../feign/ResilientRequests.java`

### Retry

Échec temporaire (réseau hiccup) → **réessayer** quelques fois.

### Circuit Breaker (coupe-circuit)

Trop d’échecs vers un service → on **arrête d’appeler** un moment (évite d’achever un service déjà down).  
Puis on reteste progressivement.

### Timeout

Si pas de réponse assez vite → on abandonne l’appel (pas d’attente infinie).

### Bulkhead

Limite le nombre d’appels **simultanés** vers un distant (isole les pannes).

### RateLimiter

Chez nous : sur `GET /full` → limite le débit des consultations enrichies.

### Comportement important

| Situation | Comportement |
|-----------|--------------|
| Création colis + distant KO | **HTTP 503** (on refuse) |
| `/full` + distant KO | Mode **dégradé** (cache / champs null) |

### ControllerAdvice

`GlobalExceptionHandler` (`@RestControllerAdvice`) = erreurs centralisées → 400 / 404 / 503 propres.  
Pas de try/catch partout dans les controllers.

### Idempotence

**Idée :** faire 2 fois la même chose ≠ créer 2 résultats.

Deux mécanismes :

1. `idempotencyKey` (header HTTP à la création)

   Même clé → `400 Opération déjà traitée`  
    → anti double-clic / retry client
2. `processed_event` (table côté consumers Kafka)

   Event déjà vu → on ignore  
    → anti rejeu Kafka / double livraison message

---

## 11. Grille = 10 points (ce qu’il faut montrer)

| # | Point | Où montrer |
|---|-------|------------|
| 1 | V0 Archi + classes + README | `README.md`, `docs/diagrams/*.png` |
| 2 | V1 Couches + DB/service + DTO | packages `entite/repository/service/controller/dto` |
| 3 | V1 CRUD + Postman | collection Postman |
| 4 | V2 Eureka UP | http://localhost:8761 |
| 5 | V2 Gateway | URL `/service-xxx/...` |
| 6 | V3 Config Server | `config-repo/` + :8888 |
| 7 | V4 Feign + validations | POST create shipment |
| 8 | V4 `/full` | GET `.../shipments/{id}/full` |
| 9 | V5 Broker + mail | livrer → Mailpit |
| 10 | V6 CB/Retry + Advice + idempotence | 503 + même clé → 400 |

---

## 12. Démo 8 minutes (si on te demande de montrer)

1. Eureka → services UP
2. Postman : customer + route OK
3. POST shipment + header `idempotencyKey` → 201 EN_ATTENTE
4. Même clé → 400
5. GET `/full` → JSON enrichi
6. depart → livrer → ouvrir Mailpit `:8025`
7. (bonus V7) Kafka UI `:8090` → topics
8. (bonus V6) stop `service-clients` → create → 503

---

## 13. Lexique express (à relire le matin)

| Mot | En une phrase |
|-----|----------------|
| Microservice | 1 responsabilité + sa base + déployable seul |
| Database per Service | Une H2 par service, pas de jointure SQL cross-service |
| Référence distante | Un `Long` (ex. `customerId`), pas une FK JPA entre bases |
| Eureka | Annuaire des instances UP |
| Gateway | Entrée unique, route par nom Eureka |
| Config Server | Config externalisée (`config-repo/`) |
| OpenFeign | Client HTTP sync typé |
| Broker | Intermédiaire de messages async |
| RabbitMQ | File classique (mail V5 sur `main`) |
| Kafka | Log d’events, rétention, multi-consommateurs (V7) |
| Publisher | Qui envoie le message |
| Consumer | Qui le reçoit |
| afterCommit | Publier seulement après commit DB |
| Idempotence | Même action ≠ 2e effet |
| Circuit Breaker | Trop d’échecs → on coupe |
| Retry | On réessaie un échec temporaire |
| DTO | Objet d’échange API, pas l’Entity |
| Mailpit | Boîte mail de test locale |

---

## 14. Phrases magiques si question surprise

- **Pourquoi pas une seule base ?** Database per Service → autonomie, pas de couplage SQL.
- **Pourquoi Feign à la création ?** Il faut savoir **tout de suite** si client/tournée OK.
- **Pourquoi broker pour le mail ?** Découplage : livraison OK même si notif down.
- **Pourquoi afterCommit ?** Éviter un event si la DB a rollback.
- **Pourquoi idempotence ?** Retry / double-clic / rejeu Kafka ne doivent pas doubler.
- **Feign vs Kafka ?** Feign = décider maintenant. Kafka = effets après.
- **Rabbit vs Kafka ?** Rabbit = file action. Kafka = journal events + plusieurs lecteurs.
- **Gateway vs Eureka ?** Eureka = qui est UP. Gateway = comment le client y accède.

---

## 15. Mini parcours code (si on te dit « montre-moi »)

| Question | Ouvre ça |
|----------|----------|
| Conception / archi | `README.md` + `docs/diagrams/architecture.png` |
| Classes / refs distantes | `docs/diagrams/classes-*.png` |
| Feign | `service-expeditions/.../feign/` |
| Création colis | `ShipmentService.create(...)` |
| Kafka publish | `ShipmentEventPublisher.java` |
| Kafka consume mail | `NotificationConsumer.java` |
| Résilience | `ResilientRequests.java` |
| Erreurs centralisées | `.../exception/GlobalExceptionHandler.java` |
| Config externalisée | `config-repo/` |
| Infra Docker | `docker-compose.yml` |

---

## 16. Ce que tu retiens en 20 secondes (récite-le)

> Quick-Ship = microservices Spring Cloud pour suivre des colis.  
> Le client passe par la **Gateway**. **Eureka** enregistre les services. **Config Server** centralise la config.  
> Chaque service a **sa base**. On lie par **IDs**, pas par FK.  
> **Feign** = sync pour valider à la création et enrichir `/full`.  
> **Broker** = async pour découpler (mail ; en V7 aussi capacité/encours/claim).  
> **V6** = Retry + Circuit Breaker + Advice + idempotence.  
> **main** = RabbitMQ ; **v7.0** = Kafka.

Tu connais ça → tu gères le contrôle.

Bonne nuit. Relis surtout les §1, 5, 6, 7, 13, 16 demain matin.
