# Références des jalons (sources professeur)

Projet d'entraînement : https://github.com/salimk/tp-ms-ecom  
PFM personnel : https://github.com/Badr0h/quick-ship

| Jalon | Sources |
|-------|---------|
| Init | https://github.com/salimk/tp-ms-ecom/blob/main/README.md |
| V1.0 | https://github.com/salimk/tp-ms-ecom/tree/v1.0 |
| V2.0 | PDF `docs/jalon v2.0/Tp-V2.0-eureka-gateway.pdf` · YouTube https://www.youtube.com/watch?v=N8M93L5p-y8 · https://github.com/salimk/tp-ms-ecom/tree/v2.0 |
| V3.0 | PDF `docs/jalon v3.0/Tp2-configserver.pdf` · YouTube https://www.youtube.com/watch?v=5JqhBn2EnuM · https://github.com/salimk/tp-ms-ecom/tree/v3.0 |
| V4.0 | PDF `docs/jalon v4.0/Tp4-openfeign.pdf` · https://github.com/salimk/tp-ms-ecom/tree/v4.0 |
| V5.0 | PDF `docs/jalon v5.0/Tp-v5-async-RabbitMQ.pdf` · https://github.com/salimk/tp-ms-ecom/tree/V5.0 |
| V6.0 | PDF `docs/jalon v6.0/Tp-v6-resilience.pdf` · https://github.com/salimk/tp-ms-ecom/tree/v6.0 |
| V7.0 | PDF `docs/jalon v7.0/Tp-v7-migration-Kafka.pdf` · `docs/jalon v7.0/docker-compose.yml` |

## Grille d'évaluation (1ère évaluation / 10 pts)

Voir `docs/Grille d'Évaluation de Projet – Contrôle (1ère Évaluation).pdf`

- V0 Conception : schéma architecture, diagramme classes, README exhaustif
- V1 CRUD + couches + DB per service + Postman
- V2/V3 Eureka + Gateway + Config Server
- V4 OpenFeign + consultation enrichie + Postman
- V5 Broker + notification (publisher/consumer) + mail
- V6 Circuit Breaker/Retry/timeout (≥2 Feign) + ControllerAdvice + idempotence

## Mapping e-com → Quick-Ship

| tp-ms-ecom | quick-ship |
|------------|------------|
| clients-service | service-clients |
| catalogue-service | service-reseau |
| commandes-service | service-expeditions |
| notification-service | service-notification |
| service-registry / gateway / config-server | idem |
