package restaurant;

/*import restaurant.database.DataRetriever;
import restaurant.models.Ingredient;
import restaurant.models.StockMovement;
import restaurant.models.Order;
import restaurant.models.Sale;
import restaurant.enums.CategoryEnum;
import restaurant.enums.MovementTypeEnum;
import restaurant.enums.PaymentStatusEnum;
import restaurant.enums.UnitEnum;
import restaurant.utils.UnitConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        DataRetriever retriever = new DataRetriever();

        System.out.println("1. TEST DE RÉCUPÉRATION D'INGRÉDIENT AVEC MOUVEMENTS:");
        testRecuperationIngredient(retriever);

        System.out.println("\n\n2. TEST CALCUL DES STOCKS À INSTANT T (TD4 point 3):");
        testCalculStocksInstantT(retriever);

        System.out.println("\n\n3. TEST saveIngredient AVEC MOUVEMENTS (TD4 point 2):");
        testSaveIngredientAvecMouvements(retriever);

        System.out.println("\n\n4. TEST DES CONVERSIONS D'UNITÉS (BONUS K1):");
        testConversionsUnites();

        System.out.println("\n\n5. TEST DES CALCULS DE STOCK AVEC CONVERSIONS:");
        testCalculStocksAvecConversions(retriever);

        System.out.println("\n\n6. TEST DES MOUVEMENTS DU BONUS:");
        testMouvementsBonus(retriever);

        System.out.println("\n\n7. TEST DES COMMANDES ET VENTES (K2):");
        testCommandesEtVentes(retriever);
    }

    private static void testRecuperationIngredient(DataRetriever retriever) {
        try {
            System.out.println("Test de récupération de l'ingrédient 'Laitue' (ID: 1)...");

            Ingredient laitue = retriever.findIngredientById(1);

            System.out.println("✓ Ingrédient récupéré: " + laitue.getName());
            System.out.println("  - Prix: " + laitue.getPrice() + " Ar");
            System.out.println("  - Catégorie: " + laitue.getCategory());
            System.out.println("  - Nombre de mouvements: " + laitue.getStockMovementList().size());

            if (!laitue.getStockMovementList().isEmpty()) {
                System.out.println("  - Détail des mouvements:");
                for (StockMovement mvt : laitue.getStockMovementList()) {
                    System.out.println("    * " + mvt.getType() + " " +
                            mvt.getQuantity() + " " + mvt.getUnit() +
                            " le " + mvt.getCreationDatetime());
                }
            }

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testCalculStocksInstantT(DataRetriever retriever) {
        try {
            System.out.println("Test des calculs de stock selon le TD4 (point 3)...");

            LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 6, 12, 0);
            Instant testInstant = testDateTime.atZone(ZoneId.systemDefault()).toInstant();

            System.out.println("Calcul des stocks à " + testDateTime + ":");

            Object[][] expectedStocks = {
                    {1, "Laitue", 4.8},
                    {2, "Tomate", 3.85},
                    {3, "Poulet", 9.0},
                    {4, "Chocolat", 2.7},
                    {5, "Beurre", 2.3}
            };

            int testsReussis = 0;

            for (Object[] expected : expectedStocks) {
                int ingredientId = (int) expected[0];
                String ingredientName = (String) expected[1];
                double expectedStock = (double) expected[2];

                try {
                    Double actualStock = retriever.getStockValueAt(ingredientId, testInstant);

                    System.out.printf("  %s (ID: %d): %.2f", ingredientName, ingredientId, actualStock);

                    if (Math.abs(actualStock - expectedStock) < 0.01) {
                        System.out.printf(" ✓ (Attendu: %.2f)\n", expectedStock);
                        testsReussis++;
                    } else {
                        System.out.printf(" ✗ (Attendu: %.2f)\n", expectedStock);
                    }

                } catch (Exception e) {
                    System.out.printf("  %s (ID: %d): Erreur - %s\n",
                            ingredientName, ingredientId, e.getMessage());
                }
            }

            System.out.printf("\nRésumé: %d/%d tests réussis\n", testsReussis, expectedStocks.length);

        } catch (Exception e) {
            System.out.println("Erreur lors du test des calculs de stock: " + e.getMessage());
        }
    }

    private static void testSaveIngredientAvecMouvements(DataRetriever retriever) {
        try {
            System.out.println("Test de saveIngredient avec ajout de mouvements...");

            String timestamp = String.valueOf(System.currentTimeMillis());
            String nomUnique = "Sucre_" + timestamp.substring(timestamp.length() - 4);

            Ingredient nouvelIngredient = new Ingredient();
            nouvelIngredient.setName(nomUnique);
            nouvelIngredient.setPrice(1200.0);
            nouvelIngredient.setCategory(CategoryEnum.OTHER);

            StockMovement entreeInitiale = new StockMovement();
            entreeInitiale.setQuantity(50.0);
            entreeInitiale.setUnit(UnitEnum.KG);
            entreeInitiale.setType(MovementTypeEnum.IN);
            entreeInitiale.setCreationDatetime(Instant.now().minusSeconds(86400 * 7));

            StockMovement sortie = new StockMovement();
            sortie.setQuantity(12.5);
            sortie.setUnit(UnitEnum.KG);
            sortie.setType(MovementTypeEnum.OUT);
            sortie.setCreationDatetime(Instant.now().minusSeconds(86400 * 2));

            nouvelIngredient.getStockMovementList().add(entreeInitiale);
            nouvelIngredient.getStockMovementList().add(sortie);

            System.out.println("Sauvegarde de l'ingrédient avec 2 mouvements...");
            Ingredient saved = retriever.saveIngredient(nouvelIngredient);

            System.out.println("✓ Ingrédient créé: " + saved.getName() + " (ID: " + saved.getId() + ")");
            System.out.println("  Nombre de mouvements sauvegardés: " + saved.getStockMovementList().size());
            System.out.println("\nTest 'on conflict do nothing' avec ID existant...");
            StockMovement mvtAvecIdExistant = new StockMovement();
            mvtAvecIdExistant.setId(6);
            mvtAvecIdExistant.setQuantity(0.5);
            mvtAvecIdExistant.setUnit(UnitEnum.KG);
            mvtAvecIdExistant.setType(MovementTypeEnum.OUT);
            mvtAvecIdExistant.setCreationDatetime(Instant.now());

            saved.getStockMovementList().add(mvtAvecIdExistant);
            int nbMouvementsAvant = saved.getStockMovementList().size();

            saved = retriever.saveIngredient(saved);
            int nbMouvementsApres = saved.getStockMovementList().size();

            if (nbMouvementsApres == nbMouvementsAvant - 1) {
                System.out.println("✓ 'On conflict do nothing' fonctionne: mouvement avec ID existant ignoré");
            } else {
                System.out.println("✗ Problème avec 'on conflict do nothing'");
            }

            System.out.println("\nTest transaction atomique simple...");
            Ingredient ingredientInvalide = new Ingredient();
            ingredientInvalide.setName(null);
            ingredientInvalide.setPrice(100.0);
            ingredientInvalide.setCategory(CategoryEnum.OTHER);

            try {
                retriever.saveIngredient(ingredientInvalide);
                System.out.println("✗ Transaction a réussi alors qu'elle devrait échouer");
            } catch (Exception e) {
                System.out.println("✓ Transaction correctement échouée: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testConversionsUnites() {
        System.out.println("=== TEST DES CONVERSIONS D'UNITÉS EN MÉMOIRE ===");

        System.out.println("\n1. Tomate:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   10 PCS -> " + UnitConverter.convert("Tomate", 10.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   5 PCS -> " + UnitConverter.convert("Tomate", 5.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.5 KG)");
        System.out.println("   1 PCS -> " + UnitConverter.convert("Tomate", 1.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.1 KG)");
        System.out.println("   Conversion de KG vers PCS:");
        System.out.println("   1 KG -> " + UnitConverter.convert("Tomate", 1.0, UnitEnum.KG, UnitEnum.PIECE) + " PCS (attendu: 10.0 PCS)");
        System.out.println("   Conversion impossible: KG vers L -> " + UnitConverter.canConvert("Tomate", UnitEnum.KG, UnitEnum.L));
        System.out.println("\n2. Laitue:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   2 PCS -> " + UnitConverter.convert("Laitue", 2.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   1 PCS -> " + UnitConverter.convert("Laitue", 1.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.5 KG)");
        System.out.println("   Conversion impossible: PCS vers L -> " + UnitConverter.canConvert("Laitue", UnitEnum.PIECE, UnitEnum.L));

        System.out.println("\n3. Chocolat:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   10 PCS -> " + UnitConverter.convert("Chocolat", 10.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   Conversion de L vers KG:");
        System.out.println("   2.5 L -> " + UnitConverter.convert("Chocolat", 2.5, UnitEnum.L, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   1 L -> " + UnitConverter.convert("Chocolat", 1.0, UnitEnum.L, UnitEnum.KG) + " KG (attendu: 0.4 KG)");
        System.out.println("   Conversion de KG vers L:");
        System.out.println("   1 KG -> " + UnitConverter.convert("Chocolat", 1.0, UnitEnum.KG, UnitEnum.L) + " L (attendu: 2.5 L)");
        System.out.println("   Conversion de PCS vers L:");
        System.out.println("   10 PCS -> " + UnitConverter.convert("Chocolat", 10.0, UnitEnum.PIECE, UnitEnum.L) + " L (attendu: 2.5 L)");

        System.out.println("\n4. Poulet:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   8 PCS -> " + UnitConverter.convert("Poulet", 8.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   4 PCS -> " + UnitConverter.convert("Poulet", 4.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.5 KG)");
        System.out.println("   Conversion impossible: PCS vers L -> " + UnitConverter.canConvert("Poulet", UnitEnum.PIECE, UnitEnum.L));

        System.out.println("\n5. Beurre:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   4 PCS -> " + UnitConverter.convert("Beurre", 4.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   Conversion de L vers KG:");
        System.out.println("   5 L -> " + UnitConverter.convert("Beurre", 5.0, UnitEnum.L, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   1 L -> " + UnitConverter.convert("Beurre", 1.0, UnitEnum.L, UnitEnum.KG) + " KG (attendu: 0.2 KG)");
        System.out.println("   Conversion de KG vers L:");
        System.out.println("   1 KG -> " + UnitConverter.convert("Beurre", 1.0, UnitEnum.KG, UnitEnum.L) + " L (attendu: 5.0 L)");
        System.out.println("   Conversion de PCS vers L:");
        System.out.println("   4 PCS -> " + UnitConverter.convert("Beurre", 4.0, UnitEnum.PIECE, UnitEnum.L) + " L (attendu: 5.0 L)");
        System.out.println("\n6. Ingrédient inconnu (Huile):");
        System.out.println("   Conversion de L vers KG -> " + UnitConverter.convert("Huile", 1.0, UnitEnum.L, UnitEnum.KG));
        System.out.println("   (Attendu: null car pas de configuration)");
    }

    private static void testCalculStocksAvecConversions(DataRetriever retriever) {
        System.out.println("=== TEST DES CALCULS DE STOCK AVEC CONVERSIONS ===");

        System.out.println("\nCalcul des stocks finaux attendus:");

        double stockLaitue = 5.0;
        double stockTomate = 4.0;
        double stockPoulet = 10.0;
        double stockChocolat = 3.0;
        double stockBeurre = 2.5;

        double sortieLaitue = UnitConverter.convertToKg("Laitue", 2.0, UnitEnum.PIECE);
        double sortieTomate = UnitConverter.convertToKg("Tomate", 5.0, UnitEnum.PIECE);
        double sortiePoulet = UnitConverter.convertToKg("Poulet", 4.0, UnitEnum.PIECE);
        double sortieChocolat = UnitConverter.convertToKg("Chocolat", 1.0, UnitEnum.L);
        double sortieBeurre = UnitConverter.convertToKg("Beurre", 1.0, UnitEnum.L);

        System.out.println("Conversions des sorties en KG:");
        System.out.printf("  Laitue: 2 PCS = %.2f KG (attendu: 1.0 KG)%n", sortieLaitue);
        System.out.printf("  Tomate: 5 PCS = %.2f KG (attendu: 0.5 KG)%n", sortieTomate);
        System.out.printf("  Poulet: 4 PCS = %.2f KG (attendu: 0.5 KG)%n", sortiePoulet);
        System.out.printf("  Chocolat: 1 L = %.2f KG (attendu: 0.4 KG)%n", sortieChocolat);
        System.out.printf("  Beurre: 1 L = %.2f KG (attendu: 0.2 KG)%n", sortieBeurre);

        double finalLaitue = stockLaitue - sortieLaitue;
        double finalTomate = stockTomate - sortieTomate;
        double finalPoulet = stockPoulet - sortiePoulet;
        double finalChocolat = stockChocolat - sortieChocolat;
        double finalBeurre = stockBeurre - sortieBeurre;

        System.out.println("\nStocks finaux calculés:");
        System.out.printf("  Laitue: %.1f - %.1f = %.1f KG%n", stockLaitue, sortieLaitue, finalLaitue);
        System.out.printf("  Tomate: %.1f - %.1f = %.1f KG%n", stockTomate, sortieTomate, finalTomate);
        System.out.printf("  Poulet: %.1f - %.1f = %.1f KG%n", stockPoulet, sortiePoulet, finalPoulet);
        System.out.printf("  Chocolat: %.1f - %.1f = %.1f KG%n", stockChocolat, sortieChocolat, finalChocolat);
        System.out.printf("  Beurre: %.1f - %.1f = %.1f KG%n", stockBeurre, sortieBeurre, finalBeurre);

        System.out.println("\nVérification avec les valeurs attendues:");
        Object[][] expected = {
                {"Laitue", 5.0, sortieLaitue, 4.0},
                {"Tomate", 4.0, sortieTomate, 3.5},
                {"Poulet", 10.0, sortiePoulet, 9.5},
                {"Chocolat", 3.0, sortieChocolat, 2.6},
                {"Beurre", 2.5, sortieBeurre, 2.3}
        };

        int correct = 0;
        for (Object[] exp : expected) {
            String nom = (String) exp[0];
            double stockInit = (double) exp[1];
            double sortieKg = (double) exp[2];
            double attendu = (double) exp[3];
            double calcule = stockInit - sortieKg;

            boolean ok = Math.abs(calcule - attendu) < 0.01;
            System.out.printf("  %s: %.1f - %.1f = %.1f KG %s (attendu: %.1f KG)%n",
                    nom, stockInit, sortieKg, calcule, ok ? "✓" : "✗", attendu);
            if (ok) correct++;
        }

        System.out.printf("\nRésumé: %d/%d calculs corrects%n", correct, expected.length);
    }

    private static void testMouvementsBonus(DataRetriever retriever) {
        System.out.println("=== SIMULATION DES MOUVEMENTS DU BONUS ===");

        System.out.println("\nSimulation des mouvements de stock:");

        System.out.println("Stocks initiaux:");
        System.out.println("  Laitue: 5.0 KG");
        System.out.println("  Tomate: 4.0 KG");
        System.out.println("  Poulet: 10.0 KG");
        System.out.println("  Chocolat: 3.0 KG");
        System.out.println("  Beurre: 2.5 KG");

        Object[][] mouvements = {
                {"Tomate", 5.0, UnitEnum.PIECE, MovementTypeEnum.OUT, "Préparation salade"},
                {"Laitue", 2.0, UnitEnum.PIECE, MovementTypeEnum.OUT, "Préparation salade"},
                {"Chocolat", 1.0, UnitEnum.L, MovementTypeEnum.OUT, "Dessert"},
                {"Poulet", 4.0, UnitEnum.PIECE, MovementTypeEnum.OUT, "Plat principal"},
                {"Beurre", 1.0, UnitEnum.L, MovementTypeEnum.OUT, "Pâtisserie"}
        };

        java.util.Map<String, Double> stocks = new java.util.HashMap<>();
        stocks.put("Laitue", 5.0);
        stocks.put("Tomate", 4.0);
        stocks.put("Poulet", 10.0);
        stocks.put("Chocolat", 3.0);
        stocks.put("Beurre", 2.5);

        System.out.println("\nApplication des mouvements:");

        for (Object[] mouvement : mouvements) {
            String ingredient = (String) mouvement[0];
            Double quantite = (Double) mouvement[1];
            UnitEnum unite = (UnitEnum) mouvement[2];
            MovementTypeEnum type = (MovementTypeEnum) mouvement[3];
            String commentaire = (String) mouvement[4];

            Double quantiteKg = UnitConverter.convertToKg(ingredient, quantite, unite);

            if (quantiteKg == null) {
                System.out.printf("  %s: Conversion impossible de %s vers KG%n", ingredient, unite);
                continue;
            }

            double stockActuel = stocks.get(ingredient);
            if (type == MovementTypeEnum.OUT) {
                stocks.put(ingredient, stockActuel - quantiteKg);
                System.out.printf("  %s: %.1f %s (%.2f KG) - %s%n",
                        ingredient, quantite, unite, quantiteKg, commentaire);
            }
        }

        System.out.println("\nRésultats finaux:");
        System.out.println("┌─────────────────┬────────────┬────────────┬─────────────┐");
        System.out.println("│ Ingrédient      │ Stock avant│ Sortie (KG)│ Stock final │");
        System.out.println("├─────────────────┼────────────┼────────────┼─────────────┤");

        Object[][] resultats = {
                {"Laitue", 5.0, 1.0, 4.0},
                {"Tomate", 4.0, 0.5, 3.5},
                {"Poulet", 10.0, 0.5, 9.5},
                {"Chocolat", 3.0, 0.4, 2.6},
                {"Beurre", 2.5, 0.2, 2.3}
        };

        int corrects = 0;
        for (Object[] res : resultats) {
            String ingredient = (String) res[0];
            double avant = (double) res[1];
            double sortie = (double) res[2];
            double attendu = (double) res[3];
            double calcule = stocks.get(ingredient);

            boolean ok = Math.abs(calcule - attendu) < 0.01;
            System.out.printf("│ %-15s │ %10.1f │ %10.1f │ %11.1f %s│%n",
                    ingredient, avant, sortie, calcule, ok ? " ✓" : " ✗");

            if (ok) corrects++;
        }

        System.out.println("└─────────────────┴────────────┴────────────┴─────────────┘");
        System.out.printf("\nValidation: %d/%d valeurs correctes%n", corrects, resultats.length);

        if (corrects == resultats.length) {
            System.out.println("✓ Tous les calculs sont conformes aux attentes du bonus!");
        } else {
            System.out.println("✗ Certains calculs sont incorrects");
        }
    }

    private static void testCommandesEtVentes(DataRetriever retriever) {
        System.out.println("=== TEST DES COMMANDES ET VENTES (K2) ===");

        try {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String referenceCommande1 = "CMD-" + uuid + "-001";
            String referenceCommande2 = "CMD-" + uuid + "-002";
            String referenceCommande3 = "CMD-" + uuid + "-003";

            System.out.println("\n1. Création d'une commande...");
            Order nouvelleCommande = new Order();
            nouvelleCommande.setReference(referenceCommande1);
            nouvelleCommande.setPaymentStatus(PaymentStatusEnum.UNPAID);

            Order commandeSauvee = retriever.saveOrder(nouvelleCommande);
            System.out.println("✓ Commande créée avec ID: " + commandeSauvee.getId() +
                    ", Référence: " + commandeSauvee.getReference() +
                    ", Statut: " + commandeSauvee.getPaymentStatus());

            System.out.println("\n2. Récupération de la commande par référence...");
            Order commandeTrouvee = retriever.findOrderByReference(referenceCommande1);
            if (commandeTrouvee != null) {
                System.out.println("✓ Commande trouvée: " + commandeTrouvee);
            } else {
                System.out.println("✗ Commande non trouvée !");
            }

            System.out.println("\n3. Marquer la commande comme payée...");
            commandeTrouvee.setPaymentStatus(PaymentStatusEnum.PAID);
            commandeTrouvee = retriever.saveOrder(commandeTrouvee);
            System.out.println("✓ Statut de paiement mis à jour: " + commandeTrouvee.getPaymentStatus());

            System.out.println("\n4. Tentative de modification d'une commande payée...");
            try {
                commandeTrouvee.setReference("CMD-MODIFIEE-" + uuid);
                retriever.saveOrder(commandeTrouvee);
                System.out.println("✗ ERREUR: La modification a réussi alors qu'elle aurait dû échouer !");
            } catch (IllegalStateException e) {
                System.out.println("✓ Exception levée correctement: " + e.getMessage());
            }

            System.out.println("\n5. Création d'une vente à partir de la commande payée...");
            Sale vente = retriever.createSaleFrom(commandeTrouvee);
            System.out.println("✓ Vente créée avec ID: " + vente.getId() +
                    ", pour la commande ID: " + vente.getOrderId() +
                    ", Date: " + vente.getSaleDatetime());

            System.out.println("\n6. Tentative de création d'une deuxième vente pour la même commande...");
            try {
                Sale vente2 = retriever.createSaleFrom(commandeTrouvee);
                System.out.println("✗ ERREUR: La deuxième vente a été créée !");
            } catch (IllegalStateException e) {
                System.out.println("✓ Exception levée correctement: " + e.getMessage());
            }

            System.out.println("\n7. Tentative de création d'une vente pour une commande non payée...");
            Order commandeNonPayee = new Order();
            commandeNonPayee.setReference(referenceCommande2);
            commandeNonPayee.setPaymentStatus(PaymentStatusEnum.UNPAID);
            commandeNonPayee = retriever.saveOrder(commandeNonPayee);
            try {
                Sale vente3 = retriever.createSaleFrom(commandeNonPayee);
                System.out.println("✗ ERREUR: La vente a été créée pour une commande non payée !");
            } catch (IllegalStateException e) {
                System.out.println("✓ Exception levée correctement: " + e.getMessage());
            }

            System.out.println("\n8. Recherche d'une commande qui n'existe pas...");
            Order commandeInexistante = retriever.findOrderByReference("CMD-INEXISTANT-" + uuid);
            if (commandeInexistante == null) {
                System.out.println("✓ Commande non trouvée (comportement attendu)");
            } else {
                System.out.println("✗ Commande trouvée alors qu'elle ne devrait pas exister");
            }

            System.out.println("\n9. Test de mise à jour d'une commande non payée...");
            Order commandePourUpdate = new Order();
            commandePourUpdate.setReference(referenceCommande3);
            commandePourUpdate.setPaymentStatus(PaymentStatusEnum.UNPAID);
            commandePourUpdate = retriever.saveOrder(commandePourUpdate);
            System.out.println("✓ Commande créée avec statut UNPAID");

            commandePourUpdate.setPaymentStatus(PaymentStatusEnum.PAID);
            commandePourUpdate = retriever.saveOrder(commandePourUpdate);
            System.out.println("✓ Commande mise à jour en PAID: ID=" + commandePourUpdate.getId());

            System.out.println("\n10. Création d'une vente pour la commande payée...");
            Sale autreVente = retriever.createSaleFrom(commandePourUpdate);
            System.out.println("✓ Vente créée pour la nouvelle commande payée: ID=" + autreVente.getId());

            System.out.println("\n11. Test de modification d'une commande non payée...");
            Order commandeModifiable = new Order();
            commandeModifiable.setReference("CMD-MOD-" + uuid);
            commandeModifiable.setPaymentStatus(PaymentStatusEnum.UNPAID);
            commandeModifiable = retriever.saveOrder(commandeModifiable);

            commandeModifiable.setReference("CMD-MOD-2-" + uuid);
            commandeModifiable = retriever.saveOrder(commandeModifiable);
            System.out.println("✓ Commande non payée modifiée avec succès");


        } catch (Exception e) {
            System.out.println("✗ Erreur lors des tests de commandes et ventes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}*/

import restaurant.database.DataRetriever;
import restaurant.models.Order;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import restaurant.database.DBConnection;

public class Main {
    public static void main(String[] args) {

        try (Connection conn = DBConnection.getDBConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM sale");
            stmt.execute("DELETE FROM order_dish");
            stmt.execute("DELETE FROM \"order\"");
            stmt.execute("INSERT INTO \"order\" (id, reference, payment_status) VALUES (1, '201', 'PAID')");
            stmt.execute("INSERT INTO sale (id, order_id) VALUES (1, 1)");
            stmt.execute("INSERT INTO \"order\" (id, reference, payment_status) VALUES (2, '202', 'UNPAID')");
        } catch (SQLException e) {
            // Ignorer
        }

        DataRetriever data = new DataRetriever();
        Order o = data.findOrderByReference("201");
        System.out.print(o);
    }
}