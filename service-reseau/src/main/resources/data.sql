INSERT INTO depot (nom, ville, capacite) VALUES ('Depot Casa Nord', 'Casablanca', 500);
INSERT INTO depot (nom, ville, capacite) VALUES ('Depot Rabat Sud', 'Rabat', 300);
INSERT INTO depot (nom, ville, capacite) VALUES ('Depot Marrakech', 'Marrakech', 200);
INSERT INTO route (nom, ville_depart, ville_arrivee, date_depart, duree_estimee, capacite_colis, capacite_reservee) VALUES ('Casa-Rabat Matin', 'Casablanca', 'Rabat', '2026-11-25', 3, 40, 0);
INSERT INTO route (nom, ville_depart, ville_arrivee, date_depart, duree_estimee, capacite_colis, capacite_reservee) VALUES ('Rabat-Fes Soir', 'Rabat', 'Fes', '2026-11-26', 5, 25, 0);
INSERT INTO route (nom, ville_depart, ville_arrivee, date_depart, duree_estimee, capacite_colis, capacite_reservee) VALUES ('Casa-Marrakech', 'Casablanca', 'Marrakech', '2026-11-27', 4, 30, 0);
