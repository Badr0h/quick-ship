# Que déposer sur Google Classroom — Quick-Ship

**Repo public :** https://github.com/Badr0h/quick-ship  
**Sujet :** Fiche 16 — Quick-Ship  
**État :** code prêt à déposer (V1→V6 sur `main`, V7 sur branche `v7.0`)

---

## Règle simple

Pour **chaque devoir Classroom**, tu colles en général :

1. le **lien du repo** : `https://github.com/Badr0h/quick-ship`
2. + le **tag ou la branche** du jalon (voir tableau)
3. + éventuellement la **collection Postman** ou une note courte

Si Classroom demande un fichier (ZIP / lien unique) : mets le lien GitHub + le tag.

---

## Tableau de dépôt (copie-colle)

| Devoir Classroom | Statut | Quoi coller / déposer |
|------------------|--------|------------------------|
| **Jalon Init** — Conception README | ✅ Fait | Lien repo + fichier `README.md` (architecture, services, cycle de vie colis) |
| **Jalon v1.0** — Services métier (CRUD) | ✅ Fait | `https://github.com/Badr0h/quick-ship/tree/v1.0` |
| **Jalon v2.0** — Eureka + Gateway | ✅ Fait | `https://github.com/Badr0h/quick-ship/tree/v2.0` |
| **Jalon v3.0** — Config Server | ✅ Fait | `https://github.com/Badr0h/quick-ship/tree/v3.0` |
| **Jalon v4.0** — OpenFeign | ✅ Fait | `https://github.com/Badr0h/quick-ship/tree/v4.0` + Postman (`postman/Quick-Ship.postman_collection.json`) |
| **Jalon v5.0** — RabbitMQ notifications | ✅ Fait | `https://github.com/Badr0h/quick-ship/tree/v5.0` *(RabbitMQ est sur `main` / tags V5–V6 ; pas sur `v7.0`)* |
| **Jalon v6.0** — Résilience + idempotence | ✅ Fait | `https://github.com/Badr0h/quick-ship/tree/v6.0` **ou** branche `main` |
| **Jalon v7.0** — Kafka | ✅ Fait | `https://github.com/Badr0h/quick-ship/tree/v7.0` **ou** tag `v7.0-release` |

> Note tags `v1.0`…`v5.0` : ils pointent vers le même socle consolidé (documenté dans le README). Pour la **démo complète V6**, préfère `v6.0` / `main`. Pour **Kafka**, uniquement `v7.0`.

---

## Texte type à coller dans Classroom

### Pour un jalon Vx.0 (exemple V6)

```
Projet Quick-Ship (Fiche 16)
Repo: https://github.com/Badr0h/quick-ship
Jalon: v6.0
Lien tag: https://github.com/Badr0h/quick-ship/tree/v6.0
Postman: postman/Quick-Ship.postman_collection.json
```

### Pour V7

```
Projet Quick-Ship (Fiche 16)
Repo: https://github.com/Badr0h/quick-ship
Jalon: v7.0 (Kafka)
Branche: https://github.com/Badr0h/quick-ship/tree/v7.0
Tag: v7.0-release
```

### Pour Init

```
Projet Quick-Ship (Fiche 16)
Repo: https://github.com/Badr0h/quick-ship
Livrable conception: README.md (+ docs/fiche-quick-ship.md)
```

---

## 1ère évaluation (grille / 10 pts) — quoi montrer

| Point grille | Où c’est |
|--------------|----------|
| V0 Conception | `README.md` |
| V1 CRUD + couches + DB per service | `service-clients`, `service-reseau` + Postman |
| V2 Eureka + Gateway | `service-registry`, `gateway` |
| V3 Config Server | `config-server` + `config-repo/` |
| V4 OpenFeign + consultation enrichie | `service-expeditions` → `/shipments/{id}/full` |
| V5 Broker + mail | RabbitMQ sur `main` + `service-notification` + Mailpit |
| V6 CB/Retry + Advice + idempotence | `ResilientRequests`, `GlobalExceptionHandler`, header `idempotencyKey` |

Lien grille : `docs/Grille d'Évaluation de Projet – Contrôle (1ère Évaluation).pdf`

---

## Comment vérifier un tag en local (si le prof demande)

```bash
git clone https://github.com/Badr0h/quick-ship.git
cd quick-ship
git checkout v6.0    # ou v7.0, v1.0, etc.
```

---

## Collaborateurs

Invitations **write** envoyées à :
- https://github.com/aymanehaska23-coder
- https://github.com/Salaheddine-Merchich  

Ils doivent accepter l’invitation GitHub pour push.

---

## Checklist avant de cliquer « Remettre »

- [ ] Repo bien **public** : https://github.com/Badr0h/quick-ship
- [ ] Tu colles le **bon tag** pour le devoir ouvert
- [ ] Pour V4/V5/V6 : mentionner aussi le fichier Postman
- [ ] Pour V7 : coller la branche `v7.0` (pas `main`)
- [ ] Tu sais expliquer en 1 min le cycle : `EN_ATTENTE → EN_TRANSIT → LIVRE/RETOUR/PERDU`
