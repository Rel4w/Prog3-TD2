package restaurant;

import restaurant.database.DataRetriever;
import restaurant.models.Ingredient;
import restaurant.models.StockMovement;
import restaurant.enums.CategoryEnum;
import restaurant.enums.MovementTypeEnum;
import restaurant.enums.UnitEnum;
import restaurant.utils.UnitConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Main {
    public static void main(String[] args) {

        DataRetriever retriever = new DataRetriever();

        // Test 1: Récupération d'un ingrédient avec ses mouvements
        System.out.println("1. TEST DE RÉCUPÉRATION D'INGRÉDIENT AVEC MOUVEMENTS:");
        testRecuperationIngredient(retriever);

        // Test 2: Calcul des stocks à un instant donné
        System.out.println("\n\n2. TEST CALCUL DES STOCKS À INSTANT T (TD4 point 3):");
        testCalculStocksInstantT(retriever);

        // Test 3: Méthode saveIngredient avec mouvements de stock
        System.out.println("\n\n3. TEST saveIngredient AVEC MOUVEMENTS (TD4 point 2):");
        testSaveIngredientAvecMouvements(retriever);

        // Test 4: Conversions d'unités (Bonus K1)
        System.out.println("\n\n4. TEST DES CONVERSIONS D'UNITÉS (BONUS K1):");
        testConversionsUnites();

        // Test 5: Calcul des stocks avec conversions (Test du bonus)
        System.out.println("\n\n5. TEST DES CALCULS DE STOCK AVEC CONVERSIONS:");
        testCalculStocksAvecConversions(retriever);

        // Test 6: Intégration avec les mouvements du bonus
        System.out.println("\n\n6. TEST DES MOUVEMENTS DU BONUS:");
        testMouvementsBonus(retriever);
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

            // 1. Créer un nouvel ingrédient avec nom UNIQUE
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nomUnique = "Sucre_" + timestamp.substring(timestamp.length() - 4);

            Ingredient nouvelIngredient = new Ingredient();
            nouvelIngredient.setName(nomUnique);
            nouvelIngredient.setPrice(1200.0);
            nouvelIngredient.setCategory(CategoryEnum.OTHER);

            // 2. Ajouter des mouvements de stock
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

            // 3. Sauvegarder
            System.out.println("Sauvegarde de l'ingrédient avec 2 mouvements...");
            Ingredient saved = retriever.saveIngredient(nouvelIngredient);

            System.out.println("✓ Ingrédient créé: " + saved.getName() + " (ID: " + saved.getId() + ")");
            System.out.println("  Nombre de mouvements sauvegardés: " + saved.getStockMovementList().size());

            // 4. Test "on conflict do nothing" avec un ID existant
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

            // 5. Test transaction atomique simple
            System.out.println("\nTest transaction atomique simple...");
            Ingredient ingredientInvalide = new Ingredient();
            ingredientInvalide.setName(null); // Nom null devrait faire échouer
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

        // Test 1: Tomate (10 PCS = 1 KG)
        System.out.println("\n1. Tomate:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   10 PCS -> " + UnitConverter.convert("Tomate", 10.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   5 PCS -> " + UnitConverter.convert("Tomate", 5.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.5 KG)");
        System.out.println("   1 PCS -> " + UnitConverter.convert("Tomate", 1.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.1 KG)");
        System.out.println("   Conversion de KG vers PCS:");
        System.out.println("   1 KG -> " + UnitConverter.convert("Tomate", 1.0, UnitEnum.KG, UnitEnum.PIECE) + " PCS (attendu: 10.0 PCS)");
        System.out.println("   Conversion impossible: KG vers L -> " + UnitConverter.canConvert("Tomate", UnitEnum.KG, UnitEnum.L));

        // Test 2: Laitue (2 PCS = 1 KG)
        System.out.println("\n2. Laitue:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   2 PCS -> " + UnitConverter.convert("Laitue", 2.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   1 PCS -> " + UnitConverter.convert("Laitue", 1.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.5 KG)");
        System.out.println("   Conversion impossible: PCS vers L -> " + UnitConverter.canConvert("Laitue", UnitEnum.PIECE, UnitEnum.L));

        // Test 3: Chocolat (10 PCS = 1 KG = 2.5 L)
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

        // Test 4: Poulet (8 PCS = 1 KG)
        System.out.println("\n4. Poulet:");
        System.out.println("   Conversion de PCS vers KG:");
        System.out.println("   8 PCS -> " + UnitConverter.convert("Poulet", 8.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 1.0 KG)");
        System.out.println("   4 PCS -> " + UnitConverter.convert("Poulet", 4.0, UnitEnum.PIECE, UnitEnum.KG) + " KG (attendu: 0.5 KG)");
        System.out.println("   Conversion impossible: PCS vers L -> " + UnitConverter.canConvert("Poulet", UnitEnum.PIECE, UnitEnum.L));

        // Test 5: Beurre (4 PCS = 1 KG = 5 L)
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

        // Test 6: Ingrédient inconnu
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

        double sortieLaitue = UnitConverter.convertToKg("Laitue", 2.0, UnitEnum.PIECE); // 2 PCS -> KG
        double sortieTomate = UnitConverter.convertToKg("Tomate", 5.0, UnitEnum.PIECE); // 5 PCS -> KG
        double sortiePoulet = UnitConverter.convertToKg("Poulet", 4.0, UnitEnum.PIECE); // 4 PCS -> KG
        double sortieChocolat = UnitConverter.convertToKg("Chocolat", 1.0, UnitEnum.L); // 1 L -> KG
        double sortieBeurre = UnitConverter.convertToKg("Beurre", 1.0, UnitEnum.L); // 1 L -> KG

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
}