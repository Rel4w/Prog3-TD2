package restaurant;

import restaurant.database.DataRetriever;
import restaurant.models.Ingredient;
import restaurant.models.StockMovement;
import restaurant.enums.CategoryEnum;
import restaurant.enums.MovementTypeEnum;
import restaurant.enums.UnitEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== TEST EXCLUSIF TD4 - GESTION DES STOCKS ===");

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
}