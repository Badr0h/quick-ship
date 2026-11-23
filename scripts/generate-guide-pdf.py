#!/usr/bin/env python3
"""Un seul PDF : Quick-Ship V0→V7 — résumé clair + diagrammes."""

from pathlib import Path

from reportlab.lib.colors import HexColor, black, white
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    HRFlowable,
    Image,
    KeepTogether,
    ListFlowable,
    ListItem,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "QUICK-SHIP-V0-V7.pdf"
DIAG = ROOT / "docs" / "diagrams"

NAVY = HexColor("#0B1F33")
TEAL = HexColor("#0E7490")
LIGHT = HexColor("#F1F5F9")
MUTED = HexColor("#475569")
OK = HexColor("#166534")

styles = getSampleStyleSheet()
for name, kwargs in [
    ("QSCover", dict(fontName="Helvetica-Bold", fontSize=20, textColor=NAVY, alignment=TA_CENTER, spaceAfter=8, leading=26)),
    ("QSCoverSub", dict(fontName="Helvetica", fontSize=10.5, textColor=MUTED, alignment=TA_CENTER, spaceAfter=4, leading=14)),
    ("QSH1", dict(fontName="Helvetica-Bold", fontSize=13, textColor=NAVY, spaceBefore=4, spaceAfter=6, leading=16)),
    ("QSH2", dict(fontName="Helvetica-Bold", fontSize=10.5, textColor=TEAL, spaceBefore=8, spaceAfter=3, leading=13)),
    ("QSBody", dict(fontName="Helvetica", fontSize=9.2, textColor=black, alignment=TA_JUSTIFY, spaceAfter=4, leading=12.5)),
    ("QSBullet", dict(fontName="Helvetica", fontSize=9, textColor=black, leftIndent=4, spaceAfter=2, leading=12)),
    ("QSCode", dict(fontName="Courier", fontSize=8, textColor=NAVY, backColor=LIGHT, leftIndent=3, spaceBefore=3, spaceAfter=3, leading=11)),
    ("QSCall", dict(fontName="Helvetica-Bold", fontSize=9.2, textColor=NAVY, spaceBefore=5, spaceAfter=5, leading=12)),
    ("QSCaption", dict(fontName="Helvetica-Oblique", fontSize=8, textColor=MUTED, alignment=TA_CENTER, spaceBefore=2, spaceAfter=8, leading=10)),
    ("QSTH", dict(fontName="Helvetica-Bold", fontSize=8, textColor=white, leading=10)),
    ("QSTD", dict(fontName="Helvetica", fontSize=8, leading=10)),
]:
    styles.add(ParagraphStyle(name, **kwargs))


def P(t, s="QSBody"):
    return Paragraph(str(t).replace("\n", "<br/>"), styles[s])


def bullets(items):
    return ListFlowable(
        [ListItem(Paragraph(i, styles["QSBullet"]), bulletColor=TEAL, leftIndent=4) for i in items],
        bulletType="bullet",
        start="•",
        leftIndent=12,
        spaceAfter=4,
    )


def hr():
    return HRFlowable(width="100%", thickness=1.2, color=TEAL, spaceBefore=2, spaceAfter=6)


def table(headers, rows, widths):
    data = [[P(h, "QSTH") for h in headers]] + [[P(c, "QSTD") for c in r] for r in rows]
    t = Table(data, colWidths=widths, repeatRows=1)
    t.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), NAVY),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [LIGHT, white]),
                ("GRID", (0, 0), (-1, -1), 0.3, HexColor("#CBD5E1")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 3),
                ("RIGHTPADDING", (0, 0), (-1, -1), 3),
                ("TOPPADDING", (0, 0), (-1, -1), 3),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
            ]
        )
    )
    return t


def fig(name, caption, max_w=16.5 * cm, max_h=9.5 * cm):
    path = DIAG / name
    if not path.exists():
        return [P(f"[Diagramme manquant : {name}]", "QSCaption")]
    img = Image(str(path))
    iw, ih = img.imageWidth, img.imageHeight
    scale = min(max_w / iw, max_h / ih, 1.0)
    img.drawWidth = iw * scale
    img.drawHeight = ih * scale
    return KeepTogether([img, P(caption, "QSCaption")])


def footer(c, doc):
    c.saveState()
    c.setFont("Helvetica", 7.5)
    c.setFillColor(MUTED)
    c.drawString(1.5 * cm, 1 * cm, "Quick-Ship — V0 → V7 (tout comprendre)")
    c.drawRightString(A4[0] - 1.5 * cm, 1 * cm, f"{doc.page}")
    c.restoreState()


def version_header(code, title, pts=""):
    label = f"{code} — {title}"
    if pts:
        label += f"  ·  {pts}"
    return KeepTogether([hr(), P(label, "QSH1")])


def build():
    story = []
    W = A4[0] - 3.0 * cm

    # ── Couverture ──
    story.append(Spacer(1, 1.4 * cm))
    story.append(P("QUICK-SHIP", "QSCover"))
    story.append(P("Tout comprendre — V0 → V7", "QSCover"))
    story.append(Spacer(1, 0.25 * cm))
    story.append(P("Fiche 16 · J2EE Avancée 2027 · 5IDSI", "QSCoverSub"))
    story.append(P("https://github.com/Badr0h/quick-ship", "QSCoverSub"))
    story.append(Spacer(1, 0.45 * cm))
    story.append(
        P(
            "Un seul document : chaque jalon dans l’ordre, résumés, diagrammes, "
            "explications orales et différences (Feign/broker, Rabbit/Kafka, Retry/CB…). "
            "Lis ce PDF = tu as le projet.",
            "QSBody",
        )
    )
    story.append(
        P(
            "<b>Branches :</b> <font face='Courier'>main / v6.0</font> = RabbitMQ (grille 1ère éval) · "
            "<font face='Courier'>v7.0</font> = Kafka",
            "QSCall",
        )
    )
    story.append(
        P(
            "<b>En 20 s :</b> Quick-Ship suit des colis en microservices Spring Cloud. "
            "Clients + réseau + expéditions, Eureka, Gateway, Config. Feign en sync, "
            "broker en async (RabbitMQ puis Kafka), résilience et idempotence.",
            "QSCall",
        )
    )

    story.append(Spacer(1, 0.3 * cm))
    story.append(P("Vue d’ensemble", "QSH2"))
    story.append(
        table(
            ["Jalon", "En une phrase", "Tag"],
            [
                ["V0", "README + architecture + diagrammes de classes", "README"],
                ["V1", "CRUD métier + couches + DB per service + Postman", "v1.0"],
                ["V2", "Eureka + Gateway (routage dynamique)", "v2.0"],
                ["V3", "Config Server (config-repo)", "v3.0"],
                ["V4", "OpenFeign + validations + /full enrichi", "v4.0"],
                ["V5", "RabbitMQ + notification mail", "v5.0"],
                ["V6", "Resilience4j + Advice + idempotence", "v6.0 / main"],
                ["V7", "Kafka event-driven (remplace RabbitMQ)", "v7.0"],
            ],
            [W * 0.10, W * 0.62, W * 0.28],
        )
    )

    story.append(Spacer(1, 0.25 * cm))
    story.append(P("Métier", "QSH2"))
    story.append(
        P(
            "Transport de colis entre entrepôts. "
            "<font face='Courier'>EN_ATTENTE → EN_TRANSIT → LIVRE | RETOUR | PERDU</font>. "
            "Retour/perdu → Claim + mail. "
            "<b>Règle :</b> 1 colis = 1 expéditeur + 1 destinataire + 1 tournée + 1 n° suivi. "
            "montantTransport = poids × 10 + 50 (serveur).",
            "QSBody",
        )
    )
    story.append(fig("cycle-colis.png", "Figure — cycle de vie du colis"))

    story.append(PageBreak())

    # ── V0 ──
    story.append(version_header("V0", "Conception & documentation d’architecture", "1 pt grille"))
    story.append(
        P(
            "<b>Attendu (grille) :</b> schéma d’architecture globale, diagramme de classes "
            "par microservice (références distantes), README exhaustif.",
            "QSBody",
        )
    )
    story.append(P("Services", "QSH2"))
    story.append(
        table(
            ["Service", "Port", "Rôle", "Entités"],
            [
                ["service-clients", "8081", "Expéditeurs + réclamations", "Customer, Claim"],
                ["service-reseau", "8082", "Dépôts + tournées + capacité", "Depot, Route"],
                ["service-expeditions *", "8083", "Colis + suivi (coeur)", "Shipment, Tracking"],
                ["service-notification", "8084", "Envoie les mails seulement", "—"],
                ["service-registry", "8761", "Eureka (annuaire)", "—"],
                ["gateway", "8080", "Entrée API unique", "—"],
                ["config-server", "8888", "Config centralisée", "config-repo/"],
            ],
            [W * 0.28, W * 0.10, W * 0.34, W * 0.28],
        )
    )
    story.append(Spacer(1, 0.15 * cm))
    story.append(fig("architecture.png", "Figure — architecture globale (Gateway, Feign, Broker, Eureka, Config)"))
    story.append(P("À dire (architecture) — résumé", "QSH2"))
    story.append(
        P(
            "Gateway = entrée unique. Eureka = annuaire des services. Config Server = config "
            "centralisée. Feign = appels sync (vérifications). Broker = async (mails / events). "
            "Chaque service a sa propre base ; on lie par IDs, pas par FK.",
            "QSCall",
        )
    )
    story.append(
        P(
            "<b>Références distantes :</b> customerId / routeId / shipmentId = Long locaux, "
            "<b>pas de FK JPA</b> entre bases (Database per Service).",
            "QSCall",
        )
    )
    story.append(fig("classes-clients.png", "Figure — classes service-clients (Claim.shipmentId = ref distante)"))
    story.append(fig("classes-reseau.png", "Figure — classes service-reseau"))
    story.append(fig("classes-expeditions.png", "Figure — classes service-expeditions (customerId / routeId = refs distantes)"))

    story.append(PageBreak())

    # ── V1 ──
    story.append(version_header("V1", "Implémentation métier & modèle de données", "2 pts grille"))
    story.append(
        P(
            "<b>Attendu :</b> couches Entity → Repo → Service → Controller ; isolation stricte "
            "des bases (clientsdb / reseaudb / expeditionsdb) ; DTO + validation ; CRUD autonome ; "
            "collection Postman pré-enregistrée.",
            "QSBody",
        )
    )
    story.append(P("Livré", "QSH2"))
    story.append(
        bullets(
            [
                "CRUD <b>Customer</b> (service-clients) + <b>Depot / Route</b> (service-reseau)",
                "Packages par couche + MapStruct DTO + Bean Validation",
                "H2 séparées → Database per Service",
                "Postman : dossier V1 Clients / Réseau",
            ]
        )
    )
    story.append(
        P(
            "<font face='Courier'>GET/POST …/service-clients/customers · …/service-reseau/depots|routes</font>",
            "QSCode",
        )
    )

    # ── V2 ──
    story.append(version_header("V2", "Service Discovery + Gateway", "2 pts (avec V3)"))
    story.append(
        P(
            "<b>Attendu :</b> toutes les apps enregistrées dans Eureka (UP, nom cohérent) ; "
            "Gateway avec routage dynamique / load balancing ; validé Postman.",
            "QSBody",
        )
    )
    story.append(
        bullets(
            [
                "Eureka : <font face='Courier'>http://localhost:8761</font> — apps UP",
                "Gateway discovery locator : URL = <font face='Courier'>/service-clients/…</font> (= nom Eureka)",
                "Plus besoin de connaître les ports métier depuis le client",
            ]
        )
    )
    story.append(
        P(
            "<b>Dire à l’oral :</b> Eureka = annuaire ; Gateway = porte d’entrée unique qui trouve les instances par nom.",
            "QSCall",
        )
    )

    # ── V3 ──
    story.append(version_header("V3", "Config Server", ""))
    story.append(
        P(
            "<b>Attendu :</b> configs externalisées et chargées depuis Config Server ; "
            "fichiers / noms cohérents avec les services.",
            "QSBody",
        )
    )
    story.append(
        bullets(
            [
                "config-server :8888 lit <font face='Courier'>config-repo/</font>",
                "Chaque service a son fichier (ex. service-expeditions.properties)",
                "Au démarrage : le service tire sa config (plus de secrets en dur dans le jar)",
            ]
        )
    )
    story.append(
        P(
            "<b>Ordre de boot :</b> config-server → registry → gateway → clients → reseau → expeditions → notification",
            "QSCode",
        )
    )

    story.append(PageBreak())

    # ── V4 ──
    story.append(version_header("V4", "Communication synchrone (OpenFeign)", "2 pts grille"))
    story.append(
        P(
            "<b>Attendu :</b> clients Feign typés + DTO ; validation des refs distantes à la création ; "
            "endpoint de consultation enrichie (DTO composé) ; Postman.",
            "QSBody",
        )
    )
    story.append(P("Création colis (Feign)", "QSH2"))
    story.append(
        bullets(
            [
                "Vérifier client ACTIF + encours / plafond (service-clients)",
                "Vérifier destination (adresse contient villeArrivee) + capacité tournée (service-reseau)",
                "Réserver 1 place avant départ",
                "Calculer montantTransport côté serveur",
                "Claim : vérifier le colis auprès de service-expeditions",
            ]
        )
    )
    story.append(P("Consultation enrichie", "QSH2"))
    story.append(
        P(
            "<font face='Courier'>GET /service-expeditions/shipments/{id}/full</font><br/>"
            "→ shipment + customer + route + tracking + claims (agrégation Feign)",
            "QSCode",
        )
    )
    story.append(
        P(
            "<b>Dire :</b> Feign = HTTP sync typé quand on a besoin de la réponse tout de suite "
            "(décision avant d’accepter le colis).",
            "QSCall",
        )
    )

    # ── V5 ──
    story.append(version_header("V5", "Asynchrone — Broker + notification", "1 pt grille"))
    story.append(
        P(
            "<b>Attendu :</b> broker (RabbitMQ) + service-notification ; Publisher + Consumer ; "
            "cycle complet : événement métier → broker → conso → mail.",
            "QSBody",
        )
    )
    story.append(
        bullets(
            [
                "Branche <font face='Courier'>main</font> : RabbitMQ",
                "Après LIVRE ou RETOUR/PERDU → publish (afterCommit) → notification → Mailpit",
                "Seul service-notification envoie le mail",
                "Découplage : la livraison reste OK même si la notif est down un moment",
            ]
        )
    )
    story.append(
        P(
            "Preuve : livrer un colis → RabbitMQ UI (queue consommée) + Mailpit :8025",
            "QSBody",
        )
    )

    story.append(PageBreak())

    # ── V6 ──
    story.append(version_header("V6", "Résilience, erreurs, idempotence", "1 pt grille"))
    story.append(
        P(
            "<b>Attendu :</b> Circuit Breaker + Retry + timeout sur ≥ 2 appels Feign ; "
            "ControllerAdvice ; idempotence des créations. (Caffeine optionnel)",
            "QSBody",
        )
    )
    story.append(P("ResilientRequests (service-expeditions)", "QSH2"))
    story.append(
        bullets(
            [
                "Timeout Feign (connexion / lecture)",
                "Retry + Circuit Breaker + Bulkhead sur <b>clients</b> ET <b>réseau</b>",
                "Création si distant KO → HTTP <b>503</b>",
                "Consultation /full : mode dégradé (cache / champs null) si besoin",
                "<b>RateLimiter</b> sur GET /full (conforme TP V6 — pas Redis Gateway)",
                "Header <b>idempotencyKey</b> : même clé → 400 « déjà traitée »",
                "ControllerAdvice → 400 / 404 / 503 centralisés",
            ]
        )
    )
    story.append(
        table(
            ["Mécanisme", "Rôle"],
            [
                ["Retry", "Réessayer un échec transitoire"],
                ["Circuit Breaker", "Trop d’échecs → on coupe les appels un moment"],
                ["Bulkhead", "Limite les appels simultanés vers un distant"],
                ["RateLimiter /full", "Limite le débit des consultations enrichies"],
                ["idempotencyKey", "Même requête ≠ 2e colis"],
            ],
            [W * 0.28, W * 0.72],
        )
    )

    # ── V7 ──
    story.append(version_header("V7", "Kafka — architecture événementielle", "hors grille 1ère éval"))
    story.append(
        P(
            "Branche <font face='Courier'>v7.0</font> : on remplace RabbitMQ par Kafka. "
            "Hybride : <b>Feign = lire / décider</b> · <b>Kafka = effets async</b> "
            "(capacité, encours, claim, mail).",
            "QSBody",
        )
    )
    story.append(fig("v7-kafka.png", "Figure — flux Kafka V7"))
    story.append(
        table(
            ["Topic / event", "De → Vers", "Effet"],
            [
                ["shipment.requested", "expeditions → reseau", "Réserver 1 capacité (atomique)"],
                ["capacity.reserved / rejected", "reseau → expeditions", "Autoriser départ ou rester EN_ATTENTE"],
                ["shipment.in-transit", "expeditions → reseau", "Valider sans 2e décompte"],
                ["shipment.delivered", "expeditions → reseau+clients+notif", "Libérer + ↑encours + mail"],
                ["parcel.returned / lost", "expeditions → reseau+clients+notif", "Libérer + 1 Claim + mail"],
                ["notification.email.requested", "expeditions → notification", "Envoi mail"],
            ],
            [W * 0.34, W * 0.34, W * 0.32],
        )
    )
    story.append(P("Contraintes à retenir", "QSH2"))
    story.append(
        bullets(
            [
                "Réserver avant départ ; libérer <b>une seule fois</b> (si réservé)",
                "Une seule Claim par shipmentId",
                "Consommateurs idempotents : table <b>processed_event</b>",
                "Seul service-notification envoie les mails",
                "Pas de writes Feign qui contournent Kafka pour capacité / encours",
            ]
        )
    )
    story.append(P("Q — Pourquoi Kafka plutôt que RabbitMQ ?", "QSH2"))
    story.append(
        P(
            "Rétention, rejeu, plusieurs consommateurs indépendants sur le même event "
            "(reseau + clients + notif). RabbitMQ = file classique OK pour le mail V5.",
            "QSBody",
        )
    )
    story.append(P("Q — Feign disparaît en V7 ?", "QSH2"))
    story.append(
        P(
            "Non. Feign reste pour lectures/décisions (client, route, /full). "
            "Kafka porte les traitements différés.",
            "QSBody",
        )
    )

    story.append(PageBreak())

    # ── Explications oral + différences ──
    story.append(hr())
    story.append(P("À dire au contrôle — explications & différences", "QSH1"))
    story.append(
        P(
            "Section à réciter. Phrases courtes, prêtes à l’oral.",
            "QSBody",
        )
    )

    story.append(P("1. Architecture globale (devant le schéma)", "QSH2"))
    story.append(
        P(
            "Quick-Ship = microservices Spring Cloud. Le client passe par la <b>Gateway</b> "
            "(porte d’entrée). <b>Eureka</b> enregistre les instances UP ; Gateway et Feign "
            "trouvent les services par nom. <b>Config Server</b> charge la config depuis "
            "config-repo. Trois services métier isolés (clients, réseau, expéditions) + "
            "notification. <b>Feign</b> = sync quand on a besoin de la réponse tout de suite. "
            "<b>Broker</b> = async (mail ; en V7 aussi capacité/encours/claim).",
            "QSBody",
        )
    )
    story.append(
        P(
            "<b>Résumé 15 s :</b> Gateway = entrée · Eureka = annuaire · Config = config "
            "centralisée · Feign = sync · Broker = async · 1 base par service, liens = IDs.",
            "QSCall",
        )
    )

    story.append(P("2. Concepts — une phrase chacun", "QSH2"))
    story.append(
        table(
            ["Concept", "À dire"],
            [
                ["Microservice", "1 responsabilité + sa base + déployable seul."],
                ["Database per Service", "H2 séparées : clientsdb / reseaudb / expeditionsdb. Pas de jointure SQL entre services."],
                ["Référence distante", "Long (customerId, routeId) dans mon service. Pas une FK JPA cross-DB. Hydraté via Feign au /full."],
                ["Eureka", "Annuaire : chaque app s’enregistre UP. On découvre par nom, pas par IP fixe."],
                ["Gateway", "Porte d’entrée unique. URL /service-clients/... = nom Eureka. Load balancing possible."],
                ["Config Server", "Config externalisée dans config-repo/, tirée au démarrage. Une source pour tous."],
                ["OpenFeign", "Client HTTP sync typé. Pour décider maintenant (client OK ? capacité OK ?)."],
                ["Broker / RabbitMQ", "File async. Publish après commit DB (afterCommit). V5/V6 = mails."],
                ["Kafka (V7)", "Events métier + notif. Rétention, rejeu, plusieurs conso sur le même event."],
                ["afterCommit", "On publie le message seulement après commit DB local → pas d’event orphelin."],
                ["Publisher / Consumer", "Un service publie l’event ; un autre le consomme et agit (ex. envoi mail)."],
                ["Idempotence", "Même requête / même event ≠ 2e colis, 2e claim, 2e mail."],
                ["idempotencyKey", "Header à la création : même clé → 400 « déjà traitée »."],
                ["processed_event", "Table V7 : un eventId déjà vu n’est pas retraité (conso idempotente)."],
                ["ControllerAdvice", "Erreurs centralisées → HTTP 400 / 404 / 503 propres."],
                ["DTO + validation", "On ne expose pas l’Entity brute ; Bean Validation sur l’entrée."],
                ["Couches", "Entity → Repo → Service → Controller. Métier dans Service, pas dans Controller."],
                ["Consultation /full", "DTO composé : shipment + customer + route + tracking + claims (Feign)."],
                ["Mailpit", "Boîte mail de test locale (:8025) pour prouver l’envoi sans vrai SMTP."],
            ],
            [W * 0.26, W * 0.74],
        )
    )

    story.append(Spacer(1, 0.2 * cm))
    story.append(P("3. Différences — le prof aime ça", "QSH2"))
    story.append(
        table(
            ["A vs B", "Différence claire"],
            [
                ["Feign vs Broker", "Feign = sync, j’attends la réponse pour décider. Broker = async, je découple (mail / effets différés)."],
                ["RabbitMQ vs Kafka", "Rabbit = file classique (V5 mail). Kafka = log d’events, rétention, multi-consommateurs (V7 métier + mail)."],
                ["main vs v7.0", "main = RabbitMQ (grille V0–V6). v7.0 = Kafka event-driven. Ne pas mélanger à la démo."],
                ["Sync vs Async", "Sync = validation avant acceptation. Async = notification / effets après le commit métier."],
                ["Retry vs Circuit Breaker", "Retry = réessayer un échec transitoire. CB = trop d’échecs → on coupe un moment."],
                ["Bulkhead vs RateLimiter", "Bulkhead = limite les appels simultanés. RateLimiter = limite le débit des /full dans le temps."],
                ["Création vs /full (panne)", "Création distant KO → 503 (on refuse). /full peut rester dégradé (cache / null)."],
                ["FK JPA vs ref distante", "FK = même base. Ref distante = Long + appel Feign/event ; chaque service garde sa DB."],
                ["Gateway vs Eureka", "Eureka = qui est UP. Gateway = comment le client y accède (routage)."],
                ["Config locale vs Config Server", "Locale = dans le jar. Config Server = externalisée, même source pour tous les services."],
                ["idempotencyKey vs processed_event", "Clé = idempotence à la création HTTP. processed_event = idempotence côté consommateur Kafka."],
                ["Réserver vs libérer capacité", "Réserver avant départ. Libérer une seule fois à la fin (LIVRE/RETOUR/PERDU), sinon stock faux."],
            ],
            [W * 0.28, W * 0.72],
        )
    )

    story.append(Spacer(1, 0.15 * cm))
    story.append(P("4. Phrases magiques (si question surprise)", "QSH2"))
    story.append(
        bullets(
            [
                "<b>Pourquoi pas une seule base ?</b> Database per Service : autonomie, pas de couplage SQL.",
                "<b>Pourquoi Feign à la création ?</b> Il faut savoir tout de suite si client/tournée sont OK.",
                "<b>Pourquoi broker pour le mail ?</b> Découplage : livraison commitée même si notif down.",
                "<b>Pourquoi afterCommit ?</b> Éviter de publier un event alors que la DB a rollback.",
                "<b>Pourquoi idempotence ?</b> Retry réseau / double-clic / rejeu Kafka ne doivent pas doubler les effets.",
                "<b>Où est le RateLimiter ?</b> Sur GET /full (Resilience4j), conforme TP V6 — pas Redis Gateway.",
                "<b>Libérer capacité une seule fois ?</b> Sinon on fausse le nombre de places sur la tournée.",
            ]
        )
    )

    story.append(PageBreak())

    # ── Grille + démo ──
    story.append(hr())
    story.append(P("Grille 1ère évaluation /10 (V0→V6)", "QSH1"))
    story.append(
        P(
            "Cette grille note <b>V0→V6</b> sur <font face='Courier'>main</font>. "
            "V7 = jalon séparé (branche v7.0).",
            "QSBody",
        )
    )
    story.append(
        table(
            ["#", "Point", "Statut", "Montrer"],
            [
                ["1", "V0 Archi + classes + README", f"<font color='{OK.hexval()}'><b>OK</b></font>", "Ce PDF + README"],
                ["2", "V1 Couches + DB/service + DTO", f"<font color='{OK.hexval()}'><b>OK</b></font>", "packages + H2"],
                ["3", "V1 CRUD + Postman", f"<font color='{OK.hexval()}'><b>OK</b></font>", "Postman V1"],
                ["4", "V2 Eureka UP", f"<font color='{OK.hexval()}'><b>OK</b></font>", ":8761"],
                ["5", "V2 Gateway", f"<font color='{OK.hexval()}'><b>OK</b></font>", "/service-xxx/…"],
                ["6", "V3 Config Server", f"<font color='{OK.hexval()}'><b>OK</b></font>", "config-repo + :8888"],
                ["7", "V4 Feign + validations", f"<font color='{OK.hexval()}'><b>OK</b></font>", "POST /shipments/new"],
                ["8", "V4 /full enrichi", f"<font color='{OK.hexval()}'><b>OK</b></font>", "GET …/full"],
                ["9", "V5 Broker + mail", f"<font color='{OK.hexval()}'><b>OK</b></font>", "livrer → Mailpit"],
                ["10", "V6 CB/Retry + Advice + idempotence", f"<font color='{OK.hexval()}'><b>OK</b></font>", "503 + clé"],
            ],
            [W * 0.06, W * 0.38, W * 0.10, W * 0.46],
        )
    )

    story.append(P("Démo main (~8 min)", "QSH2"))
    story.append(
        table(
            ["Étape", "Action", "Preuve"],
            [
                ["1", "Eureka", "services UP"],
                ["2", "Postman customer + route", "201"],
                ["3", "POST shipment + idempotencyKey", "201 EN_ATTENTE"],
                ["4", "Même clé", "400 déjà traitée"],
                ["5", "GET /full", "JSON enrichi"],
                ["6", "depart → livrer", "LIVRE + mail Mailpit"],
                ["7", "Stop clients → create", "503"],
            ],
            [W * 0.10, W * 0.50, W * 0.40],
        )
    )
    story.append(P("Démo V7 (~6 min)", "QSH2"))
    story.append(
        table(
            ["Étape", "Action", "Preuve"],
            [
                ["1", "Kafka UI :8090", "topics"],
                ["2", "create → attendre reserved", "capaciteReservee"],
                ["3", "depart → livrer/retour", "events + Claim si retour"],
                ["4", "Mailpit + processed_event", "mail + idempotence conso"],
            ],
            [W * 0.10, W * 0.50, W * 0.40],
        )
    )

    story.append(P("Démarrage", "QSH2"))
    story.append(
        P(
            "<font face='Courier' size='8'>"
            "git checkout main &amp;&amp; docker compose up -d   # V5/V6 RabbitMQ<br/>"
            "git checkout v7.0 &amp;&amp; docker compose up -d   # V7 Kafka<br/><br/>"
            "Eureka :8761 · Gateway :8080 · RabbitMQ :15672 (tp/tp123) · "
            "Kafka UI :8090 · Mailpit :8025"
            "</font>",
            "QSCode",
        )
    )
    story.append(
        P(
            "Postman : <font face='Courier'>postman/Quick-Ship.postman_collection.json</font> · "
            "Stack : Java 17 · Spring Boot 3.5.6 · Spring Cloud 2025.0.0",
            "QSCoverSub",
        )
    )

    OUT.parent.mkdir(parents=True, exist_ok=True)
    doc = SimpleDocTemplate(
        str(OUT),
        pagesize=A4,
        leftMargin=1.5 * cm,
        rightMargin=1.5 * cm,
        topMargin=1.3 * cm,
        bottomMargin=1.6 * cm,
        title="Quick-Ship V0→V7 — Tout comprendre",
        author="Quick-Ship Fiche 16",
    )
    doc.build(story, onFirstPage=footer, onLaterPages=footer)
    print(f"PDF: {OUT}")


if __name__ == "__main__":
    build()
