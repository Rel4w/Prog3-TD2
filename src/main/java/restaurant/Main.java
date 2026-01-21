package restaurant;

import restaurant.database.DataRetriever;
import restaurant.models.Dish;
import restaurant.models.Ingredient;
import restaurant.enums.CategoryEnum;

public class Main {
    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever();

        // Test 1: Vérification des coûts des plats
        System.out.println("1. Couts des plats:");

        testDishCost(retriever, 1, "Salade fraîche", 250.0);
        testDishCost(retriever, 2, "Poulet grillé", 4500.0);
        testDishCost(retriever, 3, "Riz aux légumes", 0.0);
        testDishCost(retriever, 4, "Gâteau au chocolat", 1400.0);
        testDishCost(retriever, 5, "Salade de fruits", 0.0);

        // Test 2: Vérification des marges brutes
        System.out.println("\n\n2. Marges brutes:");

        testGrossMargin(retriever, 1, "Salade fraîche", 3250.0);
        testGrossMargin(retriever, 2, "Poulet grillé", 7500.0);
        testGrossMarginException(retriever, 3, "Riz aux légumes");
        testGrossMargin(retriever, 4, "Gâteau au chocolat", 6600.0);
        testGrossMarginException(retriever, 5, "Salade de fruits");

        // Test 3: Test de réutilisation d'ingrédients
        System.out.println("\n\n3. Test de réutilisation d'ingrédients:");
        testReutilisationIngredients(retriever);
    }

    private static void testDishCost(DataRetriever retriever, int dishId,
                                     String dishName, double expectedCost) {
        try {
            Dish dish = retriever.findDishById(dishId);
            Double cost = dish.getDishCost();

            System.out.printf("%s: %.2f Ar", dishName, cost);

            if (Math.abs(cost - expectedCost) < 0.01) {
                System.out.println("(CORRECT)");
            } else {
                System.out.printf("(ATTENDU: %.2f Ar)\n", expectedCost);
            }

        } catch (Exception e) {
            System.out.printf("%s: Erreur - %s\n", dishName, e.getMessage());
        }
    }

    private static void testGrossMargin(DataRetriever retriever, int dishId,
                                        String dishName, double expectedMargin) {
        try {
            Dish dish = retriever.findDishById(dishId);
            Double margin = dish.getGrossMargin();

            System.out.printf("%s: %.2f Ar", dishName, margin);

            if (Math.abs(margin - expectedMargin) < 0.01) {
                System.out.println(" ✓ (CORRECT)");
            } else {
                System.out.printf(" ✗ (ATTENDU: %.2f Ar)\n", expectedMargin);
            }

        } catch (Exception e) {
            System.out.printf("%s: Erreur - %s\n", dishName, e.getMessage());
        }
    }

    private static void testGrossMarginException(DataRetriever retriever,
                                                 int dishId, String dishName) {
        try {
            Dish dish = retriever.findDishById(dishId);
            dish.getGrossMargin();
            System.out.printf("%s: (Devrait lever une exception pour prix NULL)\n", dishName);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Prix de vente") || e.getMessage().contains("NULL")) {
                System.out.printf("%s: Exception correctement levée: %s\n",
                        dishName, e.getMessage());
            } else {
                System.out.printf("%s: Mauvaise exception: %s\n",
                        dishName, e.getMessage());
            }
        }
    }

    private static void testReutilisationIngredients(DataRetriever retriever) {
        try {
            Ingredient tomate = new Ingredient();
            tomate.setName("Tomate Cherry");
            tomate.setPrice(800.0);
            tomate.setCategory(CategoryEnum.VEGETABLE);

            Ingredient savedTomate = retriever.saveIngredient(tomate);
            System.out.println("✓ Ingrédient unique créé: " + savedTomate.getName());

            try {
                Ingredient tomate2 = new Ingredient();
                tomate2.setName("Tomate Cherry");
                tomate2.setPrice(900.0);
                tomate2.setCategory(CategoryEnum.VEGETABLE);

                retriever.saveIngredient(tomate2);
                System.out.println("✗ Doublon créé (PROBLÈME)");
            } catch (Exception e) {
                System.out.println("✓ Doublon correctement rejeté");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du test de réutilisation: " + e.getMessage());
        }
    }
}