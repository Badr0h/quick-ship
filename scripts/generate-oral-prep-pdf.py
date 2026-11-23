#!/usr/bin/env python3
"""PDF unique — tout comprendre Quick-Ship (V0→V7) + prêt oral."""

from pathlib import Path
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.lib.colors import HexColor, white, black
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    PageBreak, ListFlowable, ListItem, HRFlowable,
)
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
import shutil

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "PRET-ORAL-1ERE-EVALUATION.pdf"
OUT2 = ROOT / "docs" / "GUIDE-TOUT-COMPRENDRE-QUICK-SHIP.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

NAVY = HexColor("#0B1F33")
TEAL = HexColor("#0E7490")
OK = "#166534"
LIGHT = HexColor("#F1F5F9")
MUTED = HexColor("#475569")

styles = getSampleStyleSheet()
for name, kwargs in [
    ("Cover", dict(fontName="Helvetica-Bold", fontSize=18, textColor=NAVY, alignment=TA_CENTER, spaceAfter=8, leading=24)),
    ("CoverSub", dict(fontName="Helvetica", fontSize=10.5, textColor=MUTED, alignment=TA_CENTER, spaceAfter=4, leading=14)),
    ("H1Oral", dict(fontName="Helvetica-Bold", fontSize=12.5, textColor=NAVY, spaceBefore=10, spaceAfter=6, leading=16)),
    ("H2Oral", dict(fontName="Helvetica-Bold", fontSize=10.2, textColor=TEAL, spaceBefore=8, spaceAfter=3, leading=13)),
    ("BodyOral", dict(fontName="Helvetica", fontSize=9, textColor=black, alignment=TA_JUSTIFY, spaceAfter=4, leading=12)),
    ("BulletOral", dict(fontName="Helvetica", fontSize=8.8, textColor=black, leftIndent=6, spaceAfter=2, leading=11.5)),
    ("CodeOral", dict(fontName="Courier", fontSize=7.5, textColor=NAVY, backColor=LIGHT, leftIndent=3, spaceBefore=2, spaceAfter=2, leading=10)),
    ("Call", dict(fontName="Helvetica-Bold", fontSize=9, textColor=NAVY, spaceBefore=5, spaceAfter=5, leading=12)),
    ("QA", dict(fontName="Helvetica-Oblique", fontSize=8.9, textColor=TEAL, spaceBefore=5, spaceAfter=1, leading=11.5)),
    ("Ans", dict(fontName="Helvetica", fontSize=8.9, textColor=black, leftIndent=5, spaceAfter=3, leading=11.5)),
    ("TH", dict(fontName="Helvetica-Bold", fontSize=7.5, textColor=white, leading=9.5)),
    ("TD", dict(fontName="Helvetica", fontSize=7.4, leading=9.5)),
]:
    styles.add(ParagraphStyle(name, **kwargs))


def P(t, s="BodyOral"):
    return Paragraph(str(t).replace("\n", "<br/>"), styles[s])


def bullets(items):
    return ListFlowable(
        [ListItem(Paragraph(i, styles["BulletOral"]), bulletColor=TEAL, leftIndent=5) for i in items],
        bulletType="bullet", start="•", leftIndent=12, spaceAfter=3,
    )


def hr():
    return HRFlowable(width="100%", thickness=1, color=TEAL, spaceBefore=2, spaceAfter=5)


def table(headers, rows, widths):
    data = [[P(h, "TH") for h in headers]] + [[P(c, "TD") for c in r] for r in rows]
    t = Table(data, colWidths=widths, repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [LIGHT, white]),
        ("GRID", (0, 0), (-1, -1), 0.3, HexColor("#CBD5E1")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 3),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3),
        ("TOPPADDING", (0, 0), (-1, -1), 2.5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 2.5),
    ]))
    return t


def footer(c, doc):
    c.saveState()
    c.setFont("Helvetica", 7.5)
    c.setFillColor(MUTED)
    c.drawString(1.5 * cm, 1 * cm, "Quick-Ship — Guide complet V0→V7 + prêt oral")
    c.drawRightString(A4[0] - 1.5 * cm, 1 * cm, f"{doc.page}")
    c.restoreState()


def build():
    story = []
    W = A4[0] - 3.0 * cm

    story.append(Spacer(1, 1.2 * cm))
    story.append(P("TOUT COMPRENDRE + PRÊT ORAL", "Cover"))
    story.append(P("Quick-Ship — Fiche 16 · V0 → V7", "Cover"))
    story.append(Spacer(1, 0.2 * cm))
    story.append(P("J2EE Avancée 2027 · 5IDSI · https://github.com/Badr0h/quick-ship", "CoverSub"))
    story.append(Spacer(1, 0.35 * cm))
    story.append(P(
        "Un seul document : le projet, les concepts, ce que le prof veut, "
        "la grille /10 (V0–V6), <b>et le V7 Kafka</b>, avec réponses orales et quoi montrer.",
        "BodyOral"))
    story.append(P(
        "<b>2 branches à ne jamais mélanger :</b><br/>"
        "• <b>main / v6.0</b> = RabbitMQ → 1ère évaluation (grille)<br/>"
        "• <b>v7.0</b> = Kafka → jalon V7",
        "Call"))
    story.append(P(
        "<b>Phrase 20 s :</b> « Quick-Ship suit des colis en microservices Spring Cloud. "
        "Clients, réseau, expéditions + Eureka, Gateway, Config. Feign en sync, "
        "RabbitMQ puis Kafka en async, résilience et idempotence. »",
        "Call"))

    story.append(PageBreak())
    story.append(hr())
    story.append(P("1. C’est quoi Quick-Ship ?", "H1Oral"))
    story.append(P(
        "App de <b>transport de colis entre entrepôts</b> : un expéditeur crée un colis sur une tournée, "
        "on le suit, puis LIVRE / RETOUR / PERDU. Retour ou perdu → Claim + e-mail.",
        "BodyOral"))
    story.append(P("<font face='Courier'>EN_ATTENTE → EN_TRANSIT → LIVRE | RETOUR | PERDU</font>", "CodeOral"))
    story.append(P("<b>Règle :</b> 1 colis = 1 expéditeur + 1 destinataire + 1 tournée + 1 n° suivi", "Call"))
    story.append(P("montantTransport = poids × 10 + 50 (calcul serveur)", "BodyOral"))

    story.append(P("Services", "H2Oral"))
    story.append(table(
        ["Service", "Port", "Rôle", "Entités"],
        [
            ["service-clients", "8081", "Expéditeurs + réclamations", "Customer, Claim"],
            ["service-reseau", "8082", "Dépôts + tournées + capacité", "Depot, Route"],
            ["service-expeditions ⭐", "8083", "Colis + suivi (cœur)", "Shipment, TrackingEntry"],
            ["service-notification", "8084", "Envoie les mails seulement", "—"],
            ["service-registry", "8761", "Eureka", "—"],
            ["gateway", "8080", "Entrée API unique", "—"],
            ["config-server", "8888", "Config centralisée", "config-repo/"],
        ],
        [W * 0.28, W * 0.10, W * 0.32, W * 0.30],
    ))
    story.append(P("Schéma mental", "H2Oral"))
    story.append(P(
        "<font face='Courier' size='7'>"
        "Postman → Gateway:8080 → clients | reseau | expeditions | notification<br/>"
        "expeditions ──Feign──► clients + reseau (lectures / décisions)<br/>"
        "expeditions ──RabbitMQ (main) ou Kafka (v7)──► notification → Mailpit<br/>"
        "Tous ↔ Eureka + Config Server"
        "</font>",
        "CodeOral"))

    story.append(hr())
    story.append(P("2. Concepts — à savoir expliquer", "H1Oral"))
    story.append(table(
        ["Concept", "En une phrase claire"],
        [
            ["Microservice", "1 responsabilité + sa base + déployable seul"],
            ["Database per Service", "H2 séparées : clientsdb / reseaudb / expeditionsdb"],
            ["Réf. distante", "Long customerId/routeId — PAS de FK JPA entre services"],
            ["Eureka", "Annuaire des instances UP ; Feign/Gateway trouvent par nom"],
            ["Gateway", "Porte d’entrée ; URL /service-clients/... = nom Eureka"],
            ["Config Server", "Config dans config-repo/, chargée au démarrage"],
            ["OpenFeign", "Appel HTTP sync typé quand on a besoin de la réponse tout de suite"],
            ["RabbitMQ (V5/V6)", "File async pour les mails (branche main)"],
            ["Kafka (V7)", "Events métier + notif ; rétention, multi-consommateurs (branche v7.0)"],
            ["afterCommit", "Publier le message seulement après commit DB local"],
            ["Idempotence", "Même requête / même event ≠ 2e colis, 2e claim, 2e mail"],
            ["Retry", "Réessayer un échec transitoire"],
            ["Circuit Breaker", "Après trop d’échecs, on coupe les appels un moment"],
            ["RateLimiter /full", "Limite les consultations enrichies (repli local)"],
            ["ControllerAdvice", "Erreurs centralisées → 400 / 404 / 503"],
        ],
        [W * 0.24, W * 0.76],
    ))

    story.append(PageBreak())
    story.append(hr())
    story.append(P("3. Tous les jalons (Init → V7)", "H1Oral"))
    story.append(table(
        ["Jalon", "Ce qu’on a fait", "Branche / tag"],
        [
            ["Init", "README + archi + cycle de vie + diagrammes classes", "README"],
            ["V1", "CRUD Customer, Depot, Route + couches + Postman", "v1.0"],
            ["V2", "Eureka + Gateway dynamique", "v2.0"],
            ["V3", "Config Server + config-repo", "v3.0"],
            ["V4", "Feign + destination/capacité + /full + Claim", "v4.0"],
            ["V5", "RabbitMQ + Publisher/Consumer + Mailpit", "v5.0 / main"],
            ["V6", "CB/Retry/timeout + Advice + idempotencyKey", "v6.0 / main"],
            ["V7", "Kafka event-driven (remplace RabbitMQ)", "v7.0 / v7.0-release"],
        ],
        [W * 0.10, W * 0.58, W * 0.32],
    ))
    story.append(P("V4 — règles Feign (création colis)", "H2Oral"))
    story.append(bullets([
        "Vérifier client ACTIF + encours/plafond",
        "Vérifier destination (adresse contient villeArrivee) + capacité tournée",
        "Réserver 1 place avant départ",
        "Calculer montant côté serveur",
        "GET /shipments/{id}/full = shipment + customer + route + tracking + claims",
    ]))
    story.append(P("V6 — résilience (classe ResilientRequests)", "H2Oral"))
    story.append(bullets([
        "Timeout Feign 1s/2s",
        "Retry + Circuit Breaker + Bulkhead sur <b>service-clients</b> ET <b>service-reseau</b>",
        "Création en panne → HTTP 503 ; /full peut rester en mode dégradé (cache/null)",
        "Header <b>idempotencyKey</b> : même clé = 400 « Opération déjà traitée »",
    ]))

    story.append(hr())
    story.append(P("4. V7 Kafka — tout comprendre", "H1Oral"))
    story.append(P(
        "On part de V6 et on <b>remplace RabbitMQ par Kafka</b>. "
        "Le projet reste hybride : <b>Feign pour lire/décider</b>, <b>Kafka pour les effets async</b>.",
        "BodyOral"))
    story.append(P(
        "<b>Checkout :</b> <font face='Courier'>git checkout v7.0</font> puis "
        "<font face='Courier'>docker compose up -d</font> (Kafka + Mailpit + Kafka UI :8090)",
        "BodyOral"))
    story.append(table(
        ["Topic / événement", "De → Vers", "Effet"],
        [
            ["shipment.requested", "expeditions → reseau", "Réserver 1 capacité (atomique)"],
            ["capacity.reserved / rejected", "reseau → expeditions", "Autoriser départ ou rester EN_ATTENTE"],
            ["shipment.in-transit", "expeditions → reseau", "Valider sans 2e décompte"],
            ["shipment.delivered", "expeditions → reseau+clients+notif", "Libérer + ↑encours + mail"],
            ["parcel.returned / lost", "expeditions → reseau+clients+notif", "Libérer + 1 Claim + mail"],
            ["notification.email.requested", "expeditions → notification", "Envoi mail Mailpit"],
        ],
        [W * 0.34, W * 0.32, W * 0.34],
    ))
    story.append(Spacer(1, 0.12 * cm))
    story.append(P("Contraintes V7 (à réciter)", "H2Oral"))
    story.append(bullets([
        "Réserver avant départ ; libérer <b>une seule fois</b> (et seulement si réservé)",
        "Une seule Claim par shipmentId",
        "Consommateurs idempotents : table <b>processed_event</b> + clé Kafka = shipmentId",
        "Seul <b>service-notification</b> envoie les mails",
        "Pas de writes Feign qui contournent Kafka pour capacité/encours",
    ]))
    story.append(P("Q — Pourquoi Kafka plutôt que RabbitMQ ?", "QA"))
    story.append(P(
        "R — Rétention, rejeu, plusieurs consommateurs indépendants sur le même event "
        "(reseau + clients + notif). RabbitMQ = file classique OK pour V5 mail simple.",
        "Ans"))
    story.append(P("Q — Feign disparaît en V7 ?", "QA"))
    story.append(P(
        "R — Non. Feign reste pour lectures/décisions (client, route, /full). "
        "Kafka porte les traitements différés (capacité, encours, claim, mail).",
        "Ans"))

    story.append(PageBreak())
    story.append(hr())
    story.append(P("5. Grille 1ère évaluation /10 — checklist", "H1Oral"))
    story.append(P(
        "Cette grille note <b>V0→V6</b>. Démo sur <b>main / v6.0</b>. "
        "V7 = hors barème de cette grille, mais <b>inclus dans ce PDF</b> pour tout comprendre.",
        "BodyOral"))
    story.append(table(
        ["#", "Point (1 pt)", "Statut", "Quoi montrer"],
        [
            ["1", "V0 Archi + classes + README", f"<font color='{OK}'><b>OK</b></font>", "README mermaid + classDiagram"],
            ["2", "V1 Couches + DB/service + DTO", f"<font color='{OK}'><b>OK</b></font>", "packages + H2 séparées"],
            ["3", "V1 CRUD + Postman", f"<font color='{OK}'><b>OK</b></font>", "Postman V1 Clients/Reseau"],
            ["4", "V2 Eureka UP", f"<font color='{OK}'><b>OK</b></font>", "http://localhost:8761"],
            ["5", "V2 Gateway dynamique", f"<font color='{OK}'><b>OK</b></font>", "URLs /service-xxx/..."],
            ["6", "V3 Config Server", f"<font color='{OK}'><b>OK</b></font>", "config-repo/ + :8888"],
            ["7", "V4 Feign + validations", f"<font color='{OK}'><b>OK</b></font>", "POST /shipments/new"],
            ["8", "V4 /full enrichi", f"<font color='{OK}'><b>OK</b></font>", "GET .../shipments/{id}/full"],
            ["9", "V5 Broker + mail", f"<font color='{OK}'><b>OK</b></font>", "livrer → Rabbit → Mailpit"],
            ["10", "V6 CB/Retry + Advice + idempotence", f"<font color='{OK}'><b>OK</b></font>", "ResilientRequests + 503 + clé"],
        ],
        [W * 0.06, W * 0.34, W * 0.10, W * 0.50],
    ))

    story.append(hr())
    story.append(P("6. Quoi dire / quoi ouvrir (oral)", "H1Oral"))
    for title, show, say in [
        ("V0", "README (schéma + diagrammes classes).", "Les refs distantes sont des IDs, pas des FK. Chaque service a sa base."),
        ("V1", "Postman create customer/depot/route + code couches.", "Entity→Repo→Service→Controller + DTO/MapStruct + validation."),
        ("V2 Eureka", "http://localhost:8761 — apps UP.", "Service Discovery : on trouve les instances par nom."),
        ("V2 Gateway", "GET http://localhost:8080/service-clients/customers", "Routage dynamique via discovery locator."),
        ("V3", "config-repo/*.properties + Config Server 8888.", "Config externalisée, une source pour tous."),
        ("V4 Feign", "POST shipment avec adresse contenant villeArrivee.", "Sync : on valide client + tournée avant d’accepter."),
        ("V4 /full", "GET .../shipments/{id}/full", "DTO composé agrégé via Feign."),
        ("V5", "livrer → Mailpit + RabbitMQ UI.", "Async : mail découplé, publish après afterCommit."),
        ("V6", "Même idempotencyKey → 400 ; stopper clients → create → 503.", "Retry/CB protègent ; Advice mappe les erreurs ; clé = idempotence."),
        ("V7", "Kafka UI topics + create→reserved→depart→livrer.", "Events métier ; capacité atomique ; processed_event ; mail via topic notif."),
    ]:
        story.append(P(f"<b>{title}</b> — montrer : {show}", "H2Oral"))
        story.append(P(f"Dire : « {say} »", "BodyOral"))

    story.append(PageBreak())
    story.append(hr())
    story.append(P("7. Deux scénarios de démo", "H1Oral"))
    story.append(P("A) Démo 1ère éval — branche main (~8 min)", "H2Oral"))
    story.append(table(
        ["Min", "Action", "Preuve"],
        [
            ["0:00", "Eureka", "5 services UP"],
            ["0:40", "Postman customer + route", "201"],
            ["1:30", "POST shipment + idempotencyKey", "201 EN_ATTENTE"],
            ["2:10", "Même clé", "400 déjà traitée"],
            ["2:40", "GET /full", "JSON enrichi"],
            ["3:30", "depart → livrer", "LIVRE"],
            ["4:20", "Mailpit", "Mail livré"],
            ["5:00", "RabbitMQ UI", "Queue consommée"],
            ["5:40", "Stop clients → create", "503"],
            ["6:40", "Questions / README classes", "Refs distantes"],
        ],
        [W * 0.10, W * 0.50, W * 0.40],
    ))
    story.append(P("B) Démo V7 Kafka — branche v7.0 (~6 min)", "H2Oral"))
    story.append(table(
        ["Min", "Action", "Preuve"],
        [
            ["0:00", "Kafka UI http://localhost:8090", "Cluster online + topics"],
            ["0:40", "Create customer + route + shipment", "EN_ATTENTE"],
            ["1:20", "Attendre capacité", "topic reserved / capaciteReservee"],
            ["2:00", "depart", "EN_TRANSIT + in-transit"],
            ["2:40", "livrer OU retour", "delivered/returned + Claim si retour"],
            ["3:40", "Mailpit", "Mail"],
            ["4:20", "Topics : requested, reserved, delivered…", "Compteurs messages"],
            ["5:00", "Expliquer processed_event + clé shipmentId", "Idempotence conso"],
        ],
        [W * 0.10, W * 0.50, W * 0.40],
    ))
    story.append(P("Démarrage", "H2Oral"))
    story.append(P(
        "<font face='Courier' size='7'>"
        "# V5/V6<br/>git checkout main &amp;&amp; docker compose up -d<br/>"
        "# V7<br/>git checkout v7.0 &amp;&amp; docker compose up -d<br/><br/>"
        "Ordre apps : config-server → registry → gateway → clients → reseau → expeditions → notification"
        "</font>",
        "CodeOral"))

    story.append(hr())
    story.append(P("8. Diagrammes de classes (oral / tableau)", "H1Oral"))
    story.append(P(
        "<font face='Courier' size='7'>"
        "clients: Customer —&lt; Claim(customerId, shipmentId*)    *→ expeditions<br/>"
        "reseau:  Depot · Route(capaciteColis, capaciteReservee)<br/>"
        "expeditions: Shipment(customerId*, routeId*, idempotencyKey) —&lt; TrackingEntry<br/>"
        "             *→ clients / reseau (IDs, pas FK JPA)"
        "</font>",
        "CodeOral"))

    story.append(hr())
    story.append(P("9. Banque Q/R (prof)", "H1Oral"))
    for q, a in [
        ("Architecture en 1 minute ?", "3 services métier + notif + Eureka + Gateway + Config. Expéditions = cœur. Feign sync, broker async."),
        ("Pourquoi pas de jointure SQL ?", "Database per Service. On agrège via Feign ou events, pas une base partagée."),
        ("Montre une référence distante.", "Shipment.customerId : Long local. Hydraté via Feign au /full."),
        ("Création d’un colis — étapes ?", "Idempotence → Feign client → Feign route/destination/capacité → montant → réserve → save EN_ATTENTE."),
        ("Pourquoi broker pour le mail ?", "Découplage : livraison commitée même si notif down. Publish afterCommit."),
        ("CB vs Retry ?", "Retry = échec transitoire. CB = trop d’échecs → on ouvre le circuit."),
        ("Idempotence ?", "idempotencyKey unique à la création. En V7 : processed_event par eventId."),
        ("Différence main et v7.0 ?", "main = RabbitMQ (V5/V6). v7.0 = Kafka events capacité/encours/claim/mail."),
        ("En V7, Feign disparaît ?", "Non : lectures/décisions en Feign ; writes métier async en Kafka."),
        ("Libérer capacité une seule fois ?", "Sinon on fausse le stock de places. V7 tracke l’affectation par shipment."),
        ("Gateway sans Eureka ?", "Plus d’instances trouvées → erreurs 503 / service unavailable."),
        ("Où est le RateLimiter ?", "Sur getFull (/full), Resilience4j — conforme TP V6."),
    ]:
        story.append(P(f"Q — {q}", "QA"))
        story.append(P(f"R — {a}", "Ans"))

    story.append(PageBreak())
    story.append(hr())
    story.append(P("10. Classroom + jour J", "H1Oral"))
    story.append(P(
        "<font face='Courier' size='7'>"
        "Projet Quick-Ship (Fiche 16)<br/>"
        "Repo: https://github.com/Badr0h/quick-ship<br/>"
        "Jalon: v6.0   Lien: https://github.com/Badr0h/quick-ship/tree/v6.0<br/>"
        "Postman: postman/Quick-Ship.postman_collection.json<br/><br/>"
        "# V7<br/>"
        "Jalon: v7.0   Branche: https://github.com/Badr0h/quick-ship/tree/v7.0"
        "</font>",
        "CodeOral"))
    story.append(P("Checklist jour J", "H2Oral"))
    story.append(bullets([
        "Savoir si le devoir = grille V6 (main) ou Kafka (v7.0)",
        "Docker UP + 7 apps + Eureka vert",
        "Postman importé (gateway http://localhost:8080)",
        "README + ce PDF ouverts",
        "Mailpit / RabbitMQ UI ou Kafka UI prêts",
        "Savoir faire le 503 (stop clients) et le doublon idempotencyKey",
        "Réciter cycle de vie + formule montant + règle capacité",
    ]))
    story.append(P("Fichiers utiles", "H2Oral"))
    story.append(table(
        ["Fichier", "Pour quoi"],
        [
            ["docs/PRET-ORAL-1ERE-EVALUATION.pdf", "CE DOCUMENT (tout V0→V7)"],
            ["docs/GUIDE-TOUT-COMPRENDRE-QUICK-SHIP.pdf", "Même contenu (nom clair)"],
            ["README.md", "Archi + diagrammes classes"],
            ["DEPOSER-CLASSROOM.md", "Liens à coller"],
            ["docs/fiche-quick-ship.md", "Énoncé Fiche 16"],
            ["postman/Quick-Ship.postman_collection.json", "Scénarios test"],
        ],
        [W * 0.48, W * 0.52],
    ))
    story.append(Spacer(1, 0.2 * cm))
    story.append(P(
        "<b>Liens live :</b> Eureka :8761 · Gateway :8080 · RabbitMQ :15672 (tp/tp123) · "
        "Kafka UI :8090 · Mailpit :8025",
        "Call"))
    story.append(P(
        "Stack : Java 17 · Spring Boot 3.5.6 · Spring Cloud 2025.0.0 · H2 · MapStruct · "
        "Resilience4j · RabbitMQ / Kafka · Mailpit",
        "CoverSub"))

    def write(path):
        doc = SimpleDocTemplate(
            str(path), pagesize=A4,
            leftMargin=1.5 * cm, rightMargin=1.5 * cm,
            topMargin=1.3 * cm, bottomMargin=1.6 * cm,
            title="Quick-Ship — Tout comprendre V0→V7",
            author="Quick-Ship Fiche 16",
        )
        doc.build(list(story), onFirstPage=footer, onLaterPages=footer)
        print(f"PDF: {path}")

    write(OUT)
    shutil.copy2(OUT, OUT2)
    print(f'PDF: {OUT2}')


if __name__ == "__main__":
    build()
