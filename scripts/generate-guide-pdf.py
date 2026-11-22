#!/usr/bin/env python3
"""Génère docs/GUIDE-COMPLET-QUICK-SHIP.pdf — guide étudiant Quick-Ship."""

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

OUT = Path(__file__).resolve().parents[1] / "docs" / "GUIDE-COMPLET-QUICK-SHIP.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

NAVY = HexColor("#0F2744")
ACCENT = HexColor("#1B6B93")
LIGHT = HexColor("#F0F4F8")
OK = HexColor("#1B7A3D")
MUTED = HexColor("#4A5568")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="CoverTitle", fontName="Helvetica-Bold", fontSize=22,
                          textColor=NAVY, alignment=TA_CENTER, spaceAfter=12, leading=28))
styles.add(ParagraphStyle(name="CoverSub", fontName="Helvetica", fontSize=12,
                          textColor=MUTED, alignment=TA_CENTER, spaceAfter=6, leading=16))
styles.add(ParagraphStyle(name="H1QS", fontName="Helvetica-Bold", fontSize=14,
                          textColor=NAVY, spaceBefore=16, spaceAfter=8, leading=18))
styles.add(ParagraphStyle(name="H2QS", fontName="Helvetica-Bold", fontSize=11,
                          textColor=ACCENT, spaceBefore=12, spaceAfter=6, leading=14))
styles.add(ParagraphStyle(name="BodyQS", fontName="Helvetica", fontSize=9.5,
                          textColor=black, alignment=TA_JUSTIFY, spaceAfter=6, leading=13))
styles.add(ParagraphStyle(name="BulletQS", fontName="Helvetica", fontSize=9.5,
                          textColor=black, leftIndent=12, spaceAfter=3, leading=12))
styles.add(ParagraphStyle(name="CodeQS", fontName="Courier", fontSize=8,
                          textColor=NAVY, backColor=LIGHT, leftIndent=6, rightIndent=6,
                          spaceBefore=4, spaceAfter=4, leading=11))
styles.add(ParagraphStyle(name="Callout", fontName="Helvetica-Bold", fontSize=9.5,
                          textColor=NAVY, alignment=TA_LEFT, spaceBefore=8, spaceAfter=8, leading=13))
styles.add(ParagraphStyle(name="Footer", fontName="Helvetica", fontSize=8,
                          textColor=MUTED, alignment=TA_CENTER))
styles.add(ParagraphStyle(name="TableCell", fontName="Helvetica", fontSize=8, leading=10))
styles.add(ParagraphStyle(name="TableHead", fontName="Helvetica-Bold", fontSize=8,
                          textColor=white, leading=10))


def P(text, style="BodyQS"):
    return Paragraph(text.replace("\n", "<br/>"), styles[style])


def bullets(items):
    return ListFlowable(
        [ListItem(Paragraph(i, styles["BulletQS"]), leftIndent=8, bulletColor=ACCENT)
         for i in items],
        bulletType="bullet", start="•", leftIndent=15, spaceBefore=2, spaceAfter=6,
    )


def section_title(n, title):
    return KeepTogether([
        HRFlowable(width="100%", thickness=1, color=ACCENT, spaceBefore=4, spaceAfter=6),
        P(f"{n}. {title}", "H1QS"),
    ])


def make_table(headers, rows, col_widths=None):
    head = [Paragraph(h, styles["TableHead"]) for h in headers]
    body = [[Paragraph(str(c), styles["TableCell"]) for c in row] for row in rows]
    data = [head] + body
    t = Table(data, colWidths=col_widths, repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("TEXTCOLOR", (0, 0), (-1, 0), white),
        ("BACKGROUND", (0, 1), (-1, -1), LIGHT),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [LIGHT, white]),
        ("GRID", (0, 0), (-1, -1), 0.4, HexColor("#CBD5E1")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]))
    return t


def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(MUTED)
    canvas.drawString(2 * cm, 1.2 * cm, "Quick-Ship — Guide complet étudiant")
    canvas.drawRightString(A4[0] - 2 * cm, 1.2 * cm, f"Page {doc.page}")
    canvas.restoreState()


def build():
    doc = SimpleDocTemplate(
        str(OUT), pagesize=A4,
        leftMargin=1.8 * cm, rightMargin=1.8 * cm,
        topMargin=1.6 * cm, bottomMargin=2 * cm,
        title="Guide Complet Quick-Ship",
        author="Projet Quick-Ship — Fiche 16",
    )
    story = []
    W = A4[0] - 3.6 * cm

    # —— COUVERTURE ——
    story.append(Spacer(1, 2.5 * cm))
    story.append(P("GUIDE COMPLET ÉTUDIANT", "CoverTitle"))
    story.append(P("Projet Quick-Ship — Fiche 16", "CoverTitle"))
    story.append(Spacer(1, 0.4 * cm))
    story.append(P("J2EE Avancée 2027 · 5IDSI · Dr. Salim KHALIL", "CoverSub"))
    story.append(P("Repo : https://github.com/Badr0h/quick-ship", "CoverSub"))
    story.append(Spacer(1, 0.8 * cm))
    story.append(P(
        "Ce document rassemble <b>tout ce qu’il faut savoir</b> : "
        "ce que le professeur attend, les concepts du cours, "
        "l’architecture du projet, les jalons Classroom, la démo, "
        "et comment déposer.",
        "BodyQS"))
    story.append(Spacer(1, 0.5 * cm))
    story.append(P(
        "<b>Phrase 30 secondes à retenir :</b><br/>"
        "« Quick-Ship est une appli microservices Spring Cloud pour le suivi de colis. "
        "Trois services métier (clients, réseau, expéditions), plus Eureka, Gateway, "
        "Config Server et notification. Feign en synchrone, RabbitMQ puis Kafka en asynchrone, "
        "avec résilience et idempotence. »",
        "Callout"))

    story.append(PageBreak())

    # —— 1. LE PROF VEUT QUOI ——
    story.append(section_title("1", "Ce que le professeur veut (clair)"))
    story.append(P(
        "Le cours construit une architecture <b>microservices Spring Cloud</b> "
        "par <b>jalons successifs</b> (comme le TP e-com du prof). "
        "Ton sujet perso est la <b>Fiche 16 — Quick-Ship</b> (logistique de colis).",
        "BodyQS"))
    story.append(P("<b>En pratique, il veut 3 choses :</b>", "H2QS"))
    story.append(bullets([
        "<b>Du code qui marche</b> pour chaque jalon (CRUD, Eureka, Feign, RabbitMQ, résilience, Kafka…).",
        "<b>Un dépôt Classroom</b> : lien GitHub + le <b>bon tag / branche</b> du jalon ouvert.",
        "<b>Savoir expliquer et démontrer</b> : cycle de vie du colis, rôle de chaque service, Feign vs broker.",
    ]))
    story.append(P(
        "La <b>1ère évaluation (/10)</b> couvre surtout <b>Init → V6</b>. "
        "Le <b>V7 Kafka</b> est la suite événementielle (devoir / branche séparée).",
        "BodyQS"))
    story.append(make_table(
        ["Point grille", "Ce qu’il vérifie", "Où c’est chez toi"],
        [
            ["V0 Conception", "README, schéma, cycle de vie", "README.md"],
            ["V1 Métier", "CRUD + couches + DB per service + Postman", "service-clients, service-reseau"],
            ["V2/V3 Infra", "Eureka UP + Gateway + Config Server", "registry, gateway, config-server"],
            ["V4 Feign", "Validations distantes + /full enrichi", "service-expeditions"],
            ["V5 Async", "Broker + publisher/consumer + mail", "RabbitMQ (main) + notification"],
            ["V6 Résilience", "CB/Retry/timeout ≥2 Feign + Advice + idempotence", "ResilientRequests, idempotencyKey"],
        ],
        col_widths=[W * 0.18, W * 0.42, W * 0.40],
    ))

    # —— 2. LE PROJET ——
    story.append(section_title("2", "Le projet Quick-Ship — de quoi on parle"))
    story.append(P(
        "Application de <b>transport de colis entre entrepôts</b> : "
        "un expéditeur crée un colis, le colis est accepté sur une tournée, "
        "suivi à chaque étape, livré avec preuve ; si retour/perdu → réclamation + e-mail.",
        "BodyQS"))
    story.append(P("<b>Règle métier clé :</b> Un Colis = 1 expéditeur + 1 destinataire + 1 tournée + 1 numéro de suivi", "Callout"))
    story.append(P("<b>Cycle de vie</b>", "H2QS"))
    story.append(P("<font face='Courier'>EN_ATTENTE → EN_TRANSIT → LIVRE / RETOUR / PERDU</font>", "CodeQS"))
    story.append(P("<b>Entités par service</b>", "H2QS"))
    story.append(make_table(
        ["Service", "Entités", "Rôle"],
        [
            ["service-clients :8081", "Customer, Claim", "Expéditeurs + réclamations"],
            ["service-reseau :8082", "Depot, Route", "Dépôts + tournées + capacité"],
            ["service-expeditions :8083 ⭐", "Shipment, TrackingEntry", "Colis + suivi (service principal)"],
            ["service-notification :8084", "—", "Envoi des e-mails uniquement"],
            ["service-registry :8761", "—", "Eureka — découverte"],
            ["gateway :8080", "—", "Entrée unique API"],
            ["config-server :8888", "—", "Config centralisée (config-repo/)"],
        ],
        col_widths=[W * 0.32, W * 0.28, W * 0.40],
    ))

    # —— 3. CONCEPTS ——
    story.append(PageBreak())
    story.append(section_title("3", "Les concepts du cours (à savoir expliquer)"))
    story.append(P(
        "Le prof n’évalue pas seulement « ça marche », il veut que tu comprennes "
        "<b>pourquoi</b> chaque brique existe.",
        "BodyQS"))

    concepts = [
        ("Microservices + Database per Service",
         "Chaque service a sa base H2 isolée. Pas de jointure SQL entre services : "
         "on garde seulement des IDs (customerId, routeId). Les données riches viennent via Feign ou événements."),
        ("Eureka (Service Discovery)",
         "Les instances s’enregistrent. La Gateway et Feign trouvent les services par nom "
         "(ex. service-clients) sans hardcoder les IPs."),
        ("API Gateway",
         "Point d’entrée unique. Routage dynamique : "
         "http://localhost:8080/service-clients/customers → service-clients."),
        ("Config Server",
         "La config vit dans config-repo/. Les apps la chargent au démarrage (port 8888). "
         "On ne change plus 7 fichiers application.properties à la main pour la même propriété."),
        ("OpenFeign (synchrone)",
         "Appel HTTP typé entre services quand on a besoin d’une réponse immédiate "
         "(vérifier client, lire une tournée, enrichir /full)."),
        ("RabbitMQ puis Kafka (asynchrone)",
         "Quand le traitement peut être différé : mail, mise à jour encours, libération capacité. "
         "V5/V6 = RabbitMQ (main). V7 = Kafka (branche v7.0). Hybrid : Feign pour lire/décider, broker pour side-effects."),
        ("Idempotence",
         "Header idempotencyKey à la création du colis : même clé = pas de double création. "
         "En V7 : table processed_event côté consommateurs Kafka pour ne pas retraiter un eventId."),
        ("Résilience (Resilience4j) — V6",
         "Timeout Feign, Retry, Circuit Breaker, Bulkhead sur ≥2 clients Feign. "
         "RateLimiter sur GET /shipments/{id}/full (repli données locales). "
         "ControllerAdvice centralise les erreurs (400/404/503…)."),
        ("Capacité tournée",
         "Avant départ on réserve 1 place. Au livrer/retour/perdu on libère <b>une seule fois</b>. "
         "En V7 la réserve est atomique (verrou) et trackée par shipment."),
    ]
    for title, body in concepts:
        story.append(P(f"<b>{title}</b>", "H2QS"))
        story.append(P(body, "BodyQS"))

    # —— 4. JALONS ——
    story.append(PageBreak())
    story.append(section_title("4", "Les jalons pas à pas (fiche + Classroom)"))
    story.append(make_table(
        ["Jalon", "Contenu attendu", "Tag / branche"],
        [
            ["Init", "README + architecture + cycle de vie", "README (repo)"],
            ["V1", "CRUD Depot, Route, Customer + couches", "tag v1.0"],
            ["V2", "Eureka + Gateway dynamique", "tag v2.0"],
            ["V3", "Config Server + config-repo", "tag v3.0"],
            ["V4", "Feign + destination/capacité + /full + Claim", "tag v4.0"],
            ["V5", "RabbitMQ + service-notification + Mailpit", "tag v5.0 (main)"],
            ["V6", "CB/Retry/timeout + Advice + idempotence + RateLimiter", "tag v6.0 / main"],
            ["V7", "Kafka event-driven (remplace RabbitMQ)", "branche v7.0 / v7.0-release"],
        ],
        col_widths=[W * 0.12, W * 0.55, W * 0.33],
    ))
    story.append(Spacer(1, 0.3 * cm))
    story.append(P("<b>Règles V4 (Feign) à connaître</b>", "H2QS"))
    story.append(bullets([
        "Vérifier compte expéditeur + encours / plafond",
        "Vérifier destination (villeArrivee) + capacité tournée",
        "Réserver capacité avant départ",
        "Calculer montantTransport = poids × 10 + 50 côté serveur",
        "Consultation enrichie : colis + expéditeur + tournée + suivi + claims",
    ]))
    story.append(P("<b>Flux V7 Kafka (à savoir dessiner)</b>", "H2QS"))
    story.append(make_table(
        ["Événement", "De → Vers", "Effet"],
        [
            ["shipment.requested", "expeditions → reseau", "Réserver 1 capacité"],
            ["capacity.reserved / rejected", "reseau → expeditions", "Autoriser départ ou attendre"],
            ["shipment.in-transit", "expeditions → reseau", "Valider sans 2e décompte"],
            ["shipment.delivered", "expeditions → reseau/clients/notif", "Libérer + ↑encours + mail"],
            ["parcel.returned / lost", "expeditions → reseau/clients/notif", "Libérer + 1 Claim + mail"],
        ],
        col_widths=[W * 0.30, W * 0.32, W * 0.38],
    ))
    story.append(P(
        "<b>Piège à éviter :</b> ne pas mélanger les branches. "
        "<b>main / v6.0 = RabbitMQ</b>. <b>v7.0 = Kafka</b>. "
        "Pour un devoir V5/V6, ne montre pas la branche Kafka.",
        "Callout"))

    # —— 5. ARCHI SCHÉMA ——
    story.append(section_title("5", "Architecture — schéma mental"))
    story.append(P(
        "<font face='Courier' size='8'>"
        "Client / Postman<br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;│<br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;▼<br/>"
        "Gateway :8080<br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;├── service-clients<br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;├── service-reseau<br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;├── service-expeditions ──Feign──► clients + reseau<br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
        "└── RabbitMQ (main) ou Kafka (v7) ──► notification ──► Mailpit<br/>"
        "Eureka ◄── tous s’enregistrent&nbsp;&nbsp;&nbsp;Config Server ◄── tous chargent la config"
        "</font>",
        "CodeQS"))
    story.append(P("<b>Interfaces utiles</b>", "H2QS"))
    story.append(bullets([
        "Eureka : http://localhost:8761",
        "RabbitMQ UI (main) : http://localhost:15672 (tp / tp123)",
        "Kafka UI (v7) : http://localhost:8090",
        "Mailpit : http://localhost:8025",
    ]))

    # —— 6. DEMARRAGE + DEMO ——
    story.append(PageBreak())
    story.append(section_title("6", "Démarrer et démontrer"))
    story.append(P("<b>Ordre de démarrage</b>", "H2QS"))
    story.append(P(
        "<font face='Courier' size='8'>"
        "docker compose up -d<br/>"
        "1. config-server (8888)<br/>"
        "2. service-registry (8761)<br/>"
        "3. gateway (8080)<br/>"
        "4. service-clients (8081)<br/>"
        "5. service-reseau (8082)<br/>"
        "6. service-expeditions (8083)<br/>"
        "7. service-notification (8084)<br/><br/>"
        "V5/V6 : git checkout main&nbsp;&nbsp;|&nbsp;&nbsp;V7 : git checkout v7.0"
        "</font>",
        "CodeQS"))
    story.append(P("<b>Scénario de démo (ordre à raconter)</b>", "H2QS"))
    story.append(bullets([
        "CRUD : créer customer + route (V1)",
        "Créer colis avec header idempotencyKey (V4 + V6)",
        "Montrer Eureka (tous UP) + appel via Gateway",
        "GET /shipments/{id}/full (agrégation Feign)",
        "depart → livrer (ou retour/perdu)",
        "Mail dans Mailpit (V5 Rabbit ou V7 Kafka)",
        "Option V6 : arrêter service-clients → création → HTTP 503 (Circuit Breaker)",
        "Option V7 : Kafka UI (topics + messages)",
    ]))
    story.append(P(
        "Collection Postman : <b>postman/Quick-Ship.postman_collection.json</b> "
        "(create, depart, livrer, retour, claims, idempotence, /full, résilience).",
        "BodyQS"))

    # —— 7. CLASSROOM ——
    story.append(section_title("7", "Que déposer sur Google Classroom"))
    story.append(P(
        "Pour <b>chaque devoir</b> : lien repo + <b>tag/branche du jalon</b> "
        "(+ Postman si V4+).",
        "BodyQS"))
    story.append(make_table(
        ["Devoir", "Lien à coller"],
        [
            ["Init", "Repo + README.md"],
            ["v1.0", "https://github.com/Badr0h/quick-ship/tree/v1.0"],
            ["v2.0", "https://github.com/Badr0h/quick-ship/tree/v2.0"],
            ["v3.0", "https://github.com/Badr0h/quick-ship/tree/v3.0"],
            ["v4.0", "…/tree/v4.0 + Postman"],
            ["v5.0", "…/tree/v5.0 (RabbitMQ = main, pas v7)"],
            ["v6.0", "…/tree/v6.0 ou branche main"],
            ["v7.0", "…/tree/v7.0 ou tag v7.0-release"],
        ],
        col_widths=[W * 0.18, W * 0.82],
    ))
    story.append(Spacer(1, 0.25 * cm))
    story.append(P("<b>Texte type V6</b>", "H2QS"))
    story.append(P(
        "<font face='Courier' size='8'>"
        "Projet Quick-Ship (Fiche 16)<br/>"
        "Repo: https://github.com/Badr0h/quick-ship<br/>"
        "Jalon: v6.0<br/>"
        "Lien: https://github.com/Badr0h/quick-ship/tree/v6.0<br/>"
        "Postman: postman/Quick-Ship.postman_collection.json"
        "</font>",
        "CodeQS"))
    story.append(P("<b>Checklist avant Remettre</b>", "H2QS"))
    story.append(bullets([
        "Repo public",
        "Bon tag pour le devoir ouvert",
        "V7 → coller v7.0 (pas main)",
        "Savoir expliquer EN_ATTENTE → EN_TRANSIT → LIVRE/RETOUR/PERDU en 1 minute",
    ]))

    # —— 8. FORMULES + FAQ ——
    story.append(section_title("8", "Formules & questions fréquentes du prof"))
    story.append(bullets([
        "<b>montantTransport</b> = poids × 10 + 50 (calcul serveur, jamais confiance au client)",
        "<b>Pourquoi Feign ET Kafka ?</b> Feign = décision immédiate / lecture. Kafka = effets différés découplés.",
        "<b>Pourquoi idempotence ?</b> Un retry réseau ne doit pas créer 2 colis ni 2 claims ni 2 mails.",
        "<b>Pourquoi libérer capacité une seule fois ?</b> Sinon on fausse le stock de places de la tournée.",
        "<b>Database per Service ?</b> Isolation : un crash / schéma d’un service n’impose pas les autres.",
    ]))

    story.append(section_title("9", "Fichiers utiles dans le repo"))
    story.append(make_table(
        ["Fichier", "Utilité"],
        [
            ["README.md", "Conception / démarrage / tags"],
            ["docs/fiche-quick-ship.md", "Énoncé Fiche 16 (ne rien inventer hors fiche+PDFs)"],
            ["docs/jalon vX.0/*.pdf", "TPs du professeur"],
            ["DEPOSER-CLASSROOM.md", "Liens à coller Classroom"],
            ["EXPLICATION-PROJET.md", "Version Markdown de l’explication"],
            ["docs/GUIDE-COMPLET-QUICK-SHIP.pdf", "CE DOCUMENT"],
            ["postman/Quick-Ship.postman_collection.json", "Scénarios de test"],
            ["config-repo/", "Configs externalisées"],
            ["docker-compose.yml", "Infra (Rabbit sur main / Kafka sur v7)"],
        ],
        col_widths=[W * 0.42, W * 0.58],
    ))

    story.append(Spacer(1, 0.6 * cm))
    story.append(P(
        "<b>État actuel :</b> code prêt (V1→V6 sur main, V7 sur v7.0). "
        "Flux Kafka testé OK (create → reserved → depart → livrer/retour + mails). "
        "Il te reste à déposer le bon tag et répéter la démo.",
        "Callout"))
    story.append(P(
        "Stack : Java 17 · Spring Boot 3.5.6 · Spring Cloud 2025.0.0 · H2 · MapStruct · "
        "Resilience4j · RabbitMQ / Kafka · Mailpit",
        "CoverSub"))

    doc.build(story, onFirstPage=footer, onLaterPages=footer)
    print(f"PDF écrit : {OUT}")
    return OUT


if __name__ == "__main__":
    build()
