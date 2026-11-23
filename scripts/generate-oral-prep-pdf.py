#!/usr/bin/env python3
"""PDF prêt oral — 1ère évaluation (grille /10) Quick-Ship."""

from pathlib import Path
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.lib.colors import HexColor, white, black
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    PageBreak, KeepTogether, ListFlowable, ListItem, HRFlowable,
)
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY

OUT = Path(__file__).resolve().parents[1] / "docs" / "PRET-ORAL-1ERE-EVALUATION.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

NAVY = HexColor("#0B1F33")
TEAL = HexColor("#0E7490")
OK_BG = HexColor("#DCFCE7")
OK_FG = HexColor("#166534")
WARN_BG = HexColor("#FEF3C7")
WARN_FG = HexColor("#92400E")
LIGHT = HexColor("#F1F5F9")
MUTED = HexColor("#475569")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle("Cover", fontName="Helvetica-Bold", fontSize=20, textColor=NAVY,
                          alignment=TA_CENTER, spaceAfter=10, leading=26))
styles.add(ParagraphStyle("CoverSub", fontName="Helvetica", fontSize=11, textColor=MUTED,
                          alignment=TA_CENTER, spaceAfter=5, leading=15))
styles.add(ParagraphStyle("H1Oral", fontName="Helvetica-Bold", fontSize=13, textColor=NAVY,
                          spaceBefore=12, spaceAfter=7, leading=17))
styles.add(ParagraphStyle("H2Oral", fontName="Helvetica-Bold", fontSize=10.5, textColor=TEAL,
                          spaceBefore=9, spaceAfter=4, leading=13))
styles.add(ParagraphStyle("BodyOral", fontName="Helvetica", fontSize=9.2, textColor=black,
                          alignment=TA_JUSTIFY, spaceAfter=5, leading=12.5))
styles.add(ParagraphStyle("BulletOral", fontName="Helvetica", fontSize=9, textColor=black,
                          leftIndent=8, spaceAfter=2, leading=12))
styles.add(ParagraphStyle("CodeOral", fontName="Courier", fontSize=7.8, textColor=NAVY,
                          backColor=LIGHT, leftIndent=4, spaceBefore=3, spaceAfter=3, leading=10.5))
styles.add(ParagraphStyle("Call", fontName="Helvetica-Bold", fontSize=9.2, textColor=NAVY,
                          spaceBefore=6, spaceAfter=6, leading=12.5))
styles.add(ParagraphStyle("QA", fontName="Helvetica-Oblique", fontSize=9.2, textColor=TEAL,
                          spaceBefore=7, spaceAfter=2, leading=12))
styles.add(ParagraphStyle("Ans", fontName="Helvetica", fontSize=9.1, textColor=black,
                          leftIndent=6, spaceAfter=4, leading=12.2))
styles.add(ParagraphStyle("TH", fontName="Helvetica-Bold", fontSize=7.8, textColor=white, leading=10))
styles.add(ParagraphStyle("TD", fontName="Helvetica", fontSize=7.7, leading=10))


def P(t, s="BodyOral"):
    return Paragraph(str(t).replace("\n", "<br/>"), styles[s])


def bullets(items):
    return ListFlowable(
        [ListItem(Paragraph(i, styles["BulletOral"]), bulletColor=TEAL, leftIndent=6) for i in items],
        bulletType="bullet", start="•", leftIndent=14, spaceAfter=4,
    )


def hr():
    return HRFlowable(width="100%", thickness=1, color=TEAL, spaceBefore=2, spaceAfter=6)


def table(headers, rows, widths):
    data = [[P(h, "TH") for h in headers]] + [[P(c, "TD") for c in r] for r in rows]
    t = Table(data, colWidths=widths, repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [LIGHT, white]),
        ("GRID", (0, 0), (-1, -1), 0.35, HexColor("#CBD5E1")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 3),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))
    return t


def footer(c, doc):
    c.saveState()
    c.setFont("Helvetica", 8)
    c.setFillColor(MUTED)
    c.drawString(1.7 * cm, 1.1 * cm, "Quick-Ship — Prêt oral 1ère évaluation")
    c.drawRightString(A4[0] - 1.7 * cm, 1.1 * cm, f"{doc.page}")
    c.restoreState()


def build():
    doc = SimpleDocTemplate(
        str(OUT), pagesize=A4,
        leftMargin=1.6 * cm, rightMargin=1.6 * cm,
        topMargin=1.4 * cm, bottomMargin=1.8 * cm,
        title="Prêt Oral — 1ère Évaluation Quick-Ship",
        author="Quick-Ship Fiche 16",
    )
    story = []
    W = A4[0] - 3.2 * cm

    # COVER
    story.append(Spacer(1, 1.8 * cm))
    story.append(P("PRÊT POUR L’ORAL", "Cover"))
    story.append(P("1ère Évaluation — Grille / 10 points", "Cover"))
    story.append(Spacer(1, 0.3 * cm))
    story.append(P("Quick-Ship · Fiche 16 · J2EE Avancée 2027", "CoverSub"))
    story.append(P("Repo : https://github.com/Badr0h/quick-ship", "CoverSub"))
    story.append(P("Branche démo : <b>main</b> / tag <b>v6.0</b> (RabbitMQ) — pas v7.0 Kafka", "CoverSub"))
    story.append(Spacer(1, 0.5 * cm))
    story.append(P(
        "Ce PDF = checklist grille + quoi montrer + réponses orales + scénario démo. "
        "Objectif : arriver devant le prof sans improvisation.",
        "BodyOral"))
    story.append(P(
        "<b>Phrase d’ouverture (20 s) :</b> « Quick-Ship suit des colis en microservices Spring Cloud. "
        "Clients / réseau / expéditions, Eureka, Gateway, Config. Feign pour les vérifs sync, "
        "RabbitMQ pour les mails, résilience et idempotence en V6. »",
        "Call"))

    # ========== CHECKLIST ==========
    story.append(PageBreak())
    story.append(hr())
    story.append(P("1. Checklist grille — est-ce que c’est là ?", "H1Oral"))
    story.append(P(
        "Barème : 1 pt = conforme et fonctionnel · 0,5 = partiel · 0 = absent. "
        "10 éléments = 10 points. Audit projet :",
        "BodyOral"))

    checklist = [
        ["#", "Élément grille", "Statut", "Preuve / quoi ouvrir"],
        ["1", "V0 Schéma architecture + README", "OK", "README.md (mermaid + démarrage)"],
        ["2", "V0 Diagrammes de classes + refs distantes", "OK", "README section Diagrammes de classes"],
        ["3", "V1 Couches Entity-Repo-Service-Controller + DTO + DB/service", "OK", "packages des 3 services + H2 clientsdb/reseaudb/expeditionsdb"],
        ["4", "V1 CRUD + Postman structuré", "OK", "Postman dossiers V1 Clients / V1 Reseau"],
        ["5", "V2 Eureka — instances UP", "OK", "http://localhost:8761 — 5 apps UP"],
        ["6", "V2 Gateway routage dynamique + Postman", "OK", "discovery.locator + URLs /service-xxx/..."],
        ["7", "V3 Config Server + config-repo", "OK", "config-server:8888 + config-repo/*.properties"],
        ["8", "V4 Feign typé + validations création", "OK", "ClientsRequest/ReseauRequest + create shipment"],
        ["9", "V4 Consultation enrichie /full + Postman", "OK", "GET .../shipments/{id}/full"],
        ["10", "V5 Broker + Publisher/Consumer + mail", "OK", "RabbitMQ + NotificationPublisher/Consumer + Mailpit"],
        ["11", "V6 CB/Retry/timeout ≥2 Feign", "OK", "ResilientRequests (clients + reseau)"],
        ["12", "V6 ControllerAdvice + idempotence", "OK", "GlobalExceptionHandler + header idempotencyKey"],
    ]
    # Grille has 10 points - I listed sub-items. Let me map to official 10:
    # Looking at grid again - phases group into ~10 deliverables:
    # 1 V0 conception
    # 2 V1 couches/DTO/DB
    # 3 V1 CRUD Postman
    # 4 V2 Eureka
    # 5 V2 Gateway
    # 6 V3 Config
    # 7 V4 Feign validation
    # 8 V4 Consultation enrichie
    # 9 V5 async mail
    # 10 V6 resilience+advice+idempotence

    story.append(table(
        ["#", "Point grille (1 pt)", "Statut", "Où / quoi montrer"],
        [
            ["1", "<b>V0</b> Archi + diagrammes classes + README",
             "<font color='#166534'><b>OK</b></font>",
             "README : flowchart + classDiagram + prérequis"],
            ["2", "<b>V1</b> Couches + DB per service + DTO/MapStruct",
             "<font color='#166534'><b>OK</b></font>",
             "Entity→Repo→Service→Controller ; H2 séparées"],
            ["3", "<b>V1</b> CRUD + collection Postman",
             "<font color='#166534'><b>OK</b></font>",
             "Customer/Depot/Route CRUD + Postman V1"],
            ["4", "<b>V2</b> Eureka — enregistrement UP",
             "<font color='#166534'><b>OK</b></font>",
             "Dashboard Eureka : GATEWAY, CLIENTS, RESEAU, EXPEDITIONS, NOTIFICATION"],
            ["5", "<b>V2</b> Gateway routage dynamique",
             "<font color='#166534'><b>OK</b></font>",
             "spring.cloud.gateway.discovery.locator.enabled=true"],
            ["6", "<b>V3</b> Config Server externalisé",
             "<font color='#166534'><b>OK</b></font>",
             "config-server + config-repo/ + spring.config.import"],
            ["7", "<b>V4</b> Feign + validation refs distantes",
             "<font color='#166534'><b>OK</b></font>",
             "create : client ACTIF, plafond, destination, capacité"],
            ["8", "<b>V4</b> Consultation enrichie (DTO composé)",
             "<font color='#166534'><b>OK</b></font>",
             "ShipmentFullDto via GET /shipments/{id}/full"],
            ["9", "<b>V5</b> Broker + notif + mail bout-en-bout",
             "<font color='#166534'><b>OK</b></font>",
             "livrer/retour → RabbitMQ → Mailpit"],
            ["10", "<b>V6</b> CB/Retry/timeout + Advice + idempotence",
             "<font color='#166534'><b>OK</b></font>",
             "ResilientRequests + GlobalExceptionHandler + idempotencyKey"],
        ],
        [W * 0.06, W * 0.34, W * 0.10, W * 0.50],
    ))
    story.append(Spacer(1, 0.25 * cm))
    story.append(P(
        "<b>Verdict audit :</b> les 10 points de la 1ère évaluation sont <b>présents et démontrables</b> "
        "sur <b>main / v6.0</b>. "
        "Attention orale : démarre bien la stack RabbitMQ (<b>pas</b> la branche Kafka v7.0 pour ce contrôle).",
        "Call"))

    # ========== PAR POINT ==========
    story.append(PageBreak())
    story.append(hr())
    story.append(P("2. Point par point — quoi ouvrir / quoi dire", "H1Oral"))

    # V0
    story.append(P("Point 1 — V0 Conception", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b> README.md (schéma mermaid + diagrammes de classes + instructions).", "BodyOral"))
    story.append(P("<b>Dire :</b> « Architecture microservices, chaque service a sa base, les refs distantes sont des IDs (customerId, routeId), pas des FK JPA. »", "BodyOral"))
    story.append(P("Q : Pourquoi pas de jointure SQL entre services ?", "QA"))
    story.append(P("R : Database per Service = isolation. On agrège via Feign (ou broker), pas via une base partagée.", "Ans"))

    # V1
    story.append(P("Point 2 & 3 — V1 Couches + CRUD", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b> arborescence d’un service + Postman Create/List customer, depot, route.", "BodyOral"))
    story.append(P(
        "<font face='Courier' size='8'>entite → repository → service → controller · dto + mapper MapStruct · validation @Valid</font>",
        "CodeOral"))
    story.append(P("Q : C’est quoi Database per Service ?", "QA"))
    story.append(P("R : clientsdb, reseaudb, expeditionsdb (H2 mémoire). Un schéma / crash / migration n’impose pas les autres.", "Ans"))

    # V2 Eureka
    story.append(P("Point 4 — Eureka", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b> http://localhost:8761 — toutes les apps <b>UP</b>.", "BodyOral"))
    story.append(P("Q : À quoi sert Eureka ?", "QA"))
    story.append(P("R : Service Discovery. Gateway et Feign trouvent les instances par nom (service-clients) sans IP en dur.", "Ans"))

    # V2 Gateway
    story.append(P("Point 5 — Gateway", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b> même requête via Gateway :", "BodyOral"))
    story.append(P(
        "<font face='Courier' size='8'>GET http://localhost:8080/service-clients/customers</font>",
        "CodeOral"))
    story.append(P("Q : Routage dynamique vs statique ?", "QA"))
    story.append(P("R : Dynamique = discovery locator activé : le préfixe d’URL = nom Eureka en minuscules. Pas besoin d’écrire une route par service à la main.", "Ans"))

    # V3
    story.append(P("Point 6 — Config Server", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b> dossier config-repo/ + Config Server 8888 + ligne spring.config.import dans une app.", "BodyOral"))
    story.append(P("Q : Pourquoi externaliser la config ?", "QA"))
    story.append(P("R : Une seule source (config-repo). On change ports/broker/resilience sans recompiler chaque module.", "Ans"))

    # V4 Feign
    story.append(P("Point 7 — OpenFeign (création)", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b> POST /shipments/new avec idempotencyKey + body valide (adresse contient villeArrivee).", "BodyOral"))
    story.append(bullets([
        "Feign → service-clients : client ACTIF + encours + plafond",
        "Feign → service-reseau : tournée + capacité + destination",
        "montantTransport = poids × 10 + 50 (serveur)",
        "réserve 1 capacité avant départ",
    ]))
    story.append(P("Q : Synchrone ou asynchrone ici ?", "QA"))
    story.append(P("R : Synchrone Feign — on a besoin de la réponse tout de suite pour accepter ou refuser la création.", "Ans"))

    # V4 full
    story.append(P("Point 8 — Consultation enrichie", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b>", "BodyOral"))
    story.append(P(
        "<font face='Courier' size='8'>GET http://localhost:8080/service-expeditions/shipments/{id}/full</font>",
        "CodeOral"))
    story.append(P("Réponse = ShipmentFullDto : shipment + customer + route + tracking + claims (agrégation Feign).", "BodyOral"))
    story.append(P("Q : Pourquoi un DTO composé ?", "QA"))
    story.append(P("R : Le front reçoit tout en 1 appel. Expéditions garde les IDs ; les détails vivent dans les autres services.", "Ans"))

    # V5
    story.append(P("Point 9 — V5 Async + mail", "H2Oral"))
    story.append(P("<b>Quoi montrer :</b> livrer un colis → logs publisher → file RabbitMQ → mail dans http://localhost:8025", "BodyOral"))
    story.append(P("Q : Pourquoi un broker plutôt que Feign pour le mail ?", "QA"))
    story.append(P("R : Le mail peut être différé. On découple : si notification est down, le métier (livraison) est déjà commit. Publisher après afterCommit.", "Ans"))

    # V6
    story.append(P("Point 10 — V6 Résilience + erreurs + idempotence", "H2Oral"))
    story.append(bullets([
        "<b>Timeout</b> Feign connect 1s / read 2s",
        "<b>Retry</b> + <b>Circuit Breaker</b> + Bulkhead sur service-clients ET service-reseau (ResilientRequests)",
        "<b>ControllerAdvice</b> : IllegalArgument→400, NotFound→404, indispo→503",
        "<b>Idempotence</b> : header idempotencyKey unique — même clé = « Opération déjà traitée »",
        "RateLimiter consultation /full (repli local) — bonus pédagogique TP",
    ]))
    story.append(P("<b>Démo choc V6 :</b> arrêter service-clients → POST /shipments/new → <b>HTTP 503</b> ; GET /full peut encore marcher avec cache/nulls.", "Call"))
    story.append(P("Q : Différence Circuit Breaker et Retry ?", "QA"))
    story.append(P("R : Retry = réessayer un échec transitoire. Circuit Breaker = après trop d’échecs, on coupe les appels un moment pour ne pas saturer le service malade.", "Ans"))

    # ========== DEMO SCRIPT ==========
    story.append(PageBreak())
    story.append(hr())
    story.append(P("3. Scénario démo chronométré (~8 minutes)", "H1Oral"))
    story.append(P("<b>Avant l’oral</b> : git checkout main (ou v6.0) · docker compose up -d · lancer les 7 apps dans l’ordre.", "BodyOral"))
    story.append(table(
        ["Min", "Action", "Preuve visuelle"],
        [
            ["0:00", "Ouvrir Eureka", "5 services UP"],
            ["0:40", "Postman : create customer + route", "201 Created"],
            ["1:30", "POST shipment + header idempotencyKey", "201 + EN_ATTENTE"],
            ["2:10", "Refaire POST même clé", "400 Opération déjà traitée"],
            ["2:40", "GET .../shipments/{id}/full", "JSON enrichi"],
            ["3:30", "POST depart puis livrer", "EN_TRANSIT → LIVRE"],
            ["4:20", "Mailpit", "Mail « Colis livré »"],
            ["5:00", "Montrer RabbitMQ UI (queue)", "Message consommé"],
            ["5:40", "Arrêter clients → create shipment", "503"],
            ["6:40", "Relancer clients → create OK", "Reprise"],
            ["7:20", "Questions / diagrammes README", "Classes + refs distantes"],
        ],
        [W * 0.10, W * 0.48, W * 0.42],
    ))

    story.append(P("Ordre démarrage", "H2Oral"))
    story.append(P(
        "<font face='Courier' size='8'>"
        "docker compose up -d<br/>"
        "config-server → service-registry → gateway → clients → reseau → expeditions → notification"
        "</font>",
        "CodeOral"))

    # ========== CONCEPTS CHEAT ==========
    story.append(hr())
    story.append(P("4. Aide-mémoire concepts (réponses courtes)", "H1Oral"))
    story.append(table(
        ["Concept", "En une phrase"],
        [
            ["Microservice", "Un service = une responsabilité + sa propre base + déployable seul"],
            ["Eureka", "Annuaire des instances vivantes"],
            ["Gateway", "Porte d’entrée unique + routage"],
            ["Config Server", "Config centralisée versionnée"],
            ["OpenFeign", "Client HTTP typé sync entre services"],
            ["RabbitMQ", "File de messages async (V5/V6)"],
            ["Idempotence", "Rejouer la même requête ne crée pas un 2e effet"],
            ["Circuit Breaker", "Coupe le circuit si le distant est en panne"],
            ["afterCommit", "Publier le message seulement après commit DB local"],
            ["Réf. distante", "Long customerId/routeId — pas de @ManyToOne cross-DB"],
        ],
        [W * 0.22, W * 0.78],
    ))

    # ========== FORMULES ==========
    story.append(P("Formules / règles métier à réciter", "H2Oral"))
    story.append(bullets([
        "Colis = 1 expéditeur + 1 destinataire + 1 tournée + 1 n° suivi",
        "Cycle : EN_ATTENTE → EN_TRANSIT → LIVRE | RETOUR | PERDU",
        "montantTransport = poids × 10 + 50",
        "Réserver capacité avant départ ; libérer <b>une seule fois</b> à la fin",
        "Retour/Perdu → une seule Claim",
    ]))

    # ========== CLASS DIAGRAMS TEXT ==========
    story.append(PageBreak())
    story.append(hr())
    story.append(P("5. Diagrammes de classes — version orale", "H1Oral"))
    story.append(P(
        "À dessiner au tableau si demandé. Les flèches pointillées = références distantes (ID), pas JPA.",
        "BodyOral"))
    story.append(P(
        "<font face='Courier' size='7.5'>"
        "service-clients<br/>"
        "  Customer(id, nom, email, encours, plafondCredit, statut)<br/>"
        "  Claim(id, customerId, shipmentId*, description, montant, statut)<br/>"
        "  *shipmentId → service-expeditions<br/><br/>"
        "service-reseau<br/>"
        "  Depot(id, nom, ville, capacite)<br/>"
        "  Route(id, nom, villeDepart, villeArrivee, dateDepart, dureeEstimee, capaciteColis, capaciteReservee)<br/><br/>"
        "service-expeditions<br/>"
        "  Shipment(id, customerId*, destinataireNom, adresseDestinataire, poids, routeId*, montantTransport, numeroSuivi, statut, idempotencyKey)<br/>"
        "  TrackingEntry(id, shipmentId, date, localisation, commentaire)<br/>"
        "  *customerId → service-clients · *routeId → service-reseau"
        "</font>",
        "CodeOral"))

    # ========== Q BANK ==========
    story.append(hr())
    story.append(P("6. Banque de questions probables + réponses", "H1Oral"))

    qa = [
        ("Explique ton architecture en 1 minute.",
         "3 services métier + notification + Eureka + Gateway + Config. Expéditions est le cœur. Sync Feign, async RabbitMQ pour les mails."),
        ("Montre-moi une référence distante.",
         "Shipment.customerId : Long stocké localement. Pas de @ManyToOne vers Customer. On hydrate via Feign au /full."),
        ("Que se passe-t-il à la création d’un colis ?",
         "Vérif idempotence → Feign client → Feign route/destination/capacité → calcul montant → réserve capacité → save EN_ATTENTE."),
        ("Comment testes-tu ?",
         "Collection Postman versionnée : CRUD V1, flux V4, idempotence/résilience V6, mails après livrer."),
        ("Différence V5 et V7 ?",
         "V5 = RabbitMQ (évalué ici sur main). V7 = Kafka event-driven sur branche v7.0 (hors focus si le contrôle s’arrête à V6)."),
        ("Où est le RateLimiter ?",
         "Sur getFull (consultation enrichie), Resilience4j, conforme TP V6 — pas Redis Gateway."),
        ("Comment évites-tu les doubles mails / doubles claims ?",
         "afterCommit + en V6 logique métier ; Claim unique par shipmentId ; en V7 processed_event."),
        ("Gateway sans Eureka ?",
         "Le discovery locator ne trouverait plus les services → 503 / unable to find instance."),
    ]
    for q, a in qa:
        story.append(P(f"Q — {q}", "QA"))
        story.append(P(f"R — {a}", "Ans"))

    # ========== CHECKLIST JOUR J ==========
    story.append(Spacer(1, 0.3 * cm))
    story.append(hr())
    story.append(P("7. Checklist jour J", "H1Oral"))
    story.append(bullets([
        "Branche <b>main</b> / tag <b>v6.0</b> (RabbitMQ)",
        "Docker UP (RabbitMQ + Mailpit)",
        "7 apps démarrées, Eureka vert",
        "Postman collection importée, variables gateway=http://localhost:8080",
        "README ouvert (architecture + diagrammes classes)",
        "Mailpit onglet prêt ; savoir arrêter service-clients pour le 503",
        "Ce PDF / EXPLICATION-PROJET.md sous la main",
    ]))
    story.append(P(
        "<b>Liens rapides :</b> Eureka 8761 · Gateway 8080 · RabbitMQ 15672 (tp/tp123) · Mailpit 8025 · "
        "Postman postman/Quick-Ship.postman_collection.json · Grille docs/Grille d'Évaluation…pdf",
        "Call"))
    story.append(P(
        "Document généré pour la 1ère évaluation. V7 Kafka = suite (autre branche) — à mentionner seulement si le prof demande.",
        "CoverSub"))

    doc.build(story, onFirstPage=footer, onLaterPages=footer)
    print(f"PDF: {OUT}")
    return OUT


if __name__ == "__main__":
    build()

