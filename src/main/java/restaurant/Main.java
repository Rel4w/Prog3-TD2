package restaurant;

import restaurant.database.DataRetriever;
import restaurant.models.Dish;
import restaurant.models.Ingredient;
import restaurant.enums.CategoryEnum;
import restaurant.enums.DishTypeEnum;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever();

        try {
            // Test1
            System.out.println("\nTest a) findDishById(1):");
            Dish dish1 = retriever.findDishById(1);
            System.out.println("Résultat: " + dish1.getName() + " avec " + dish1.getIngredients().size() + " ingrédients");

            // Test2
            System.out.println("\nTest b) findDishById(999):");
            try {
                retriever.findDishById(999);
                System.out.println("ERREUR: Devrait lever une exception!");
            } catch (RuntimeException e) {
                System.out.println("Exception attendue: " + e.getMessage());
            }

            // Test3
            System.out.println("\nTest c) findIngredients(page=2, size=2):");
            List<Ingredient> page2 = retriever.findIngredients(2, 2);
            System.out.println("Résultats attendus: Poulet, Chocolat");
            System.out.println("Résultats obtenus:");
            page2.forEach(i -> System.out.println("  - " + i.getName()));

            // Test4
            System.out.println("\nTest d) findIngredients(page=3, size=5):");
            List<Ingredient> page3 = retriever.findIngredients(3, 5);
            System.out.println("Résultats attendus: Liste vide");
            System.out.println("Taille: " + page3.size());

            // Test5
            System.out.println("\nTest e) findDishsByIngredientName('eur'):");
            List<Dish> dishesWithEur = retriever.findDishsByIngredientName("eur");
            System.out.println("Résultat attendu: Plat - Gâteau au chocolat");
            System.out.println("Résultats obtenus:");
            dishesWithEur.forEach(d -> System.out.println("  - " + d.getName()));

            // Test6
            System.out.println("\nTest f) findIngredientsByCriteria:");
            System.out.println("Paramètres: ingredientName=null, category=VEGETABLE, dishName=null, page=1, size=10");
            System.out.println("Résultat attendu: Laitue, Tomate");
            List<Ingredient> vegetables = retriever.findIngredientsByCriteria(
                    null, CategoryEnum.VEGETABLE, null, 1, 10
            );
            System.out.println("Résultats obtenus:");
            vegetables.forEach(i -> System.out.println("  - " + i.getName()));

            // Test7
            System.out.println("\nTest g) findIngredientsByCriteria:");
            System.out.println("Paramètres: ingredientName='cho', category=null, dishName='Sal', page=1, size=10");
            System.out.println("Résultat attendu: Liste vide");
            List<Ingredient> emptyResult = retriever.findIngredientsByCriteria(
                    "cho", null, "Sal", 1, 10
            );
            System.out.println("Résultats: " + emptyResult.size());

            // Test8
            System.out.println("\nTest h) findIngredientsByCriteria:");
            System.out.println("Paramètres: ingredientName='cho', category=null, dishName='gâteau', page=1, size=10");
            System.out.println("Résultat attendu: Chocolat");
            List<Ingredient> chocolatResult = retriever.findIngredientsByCriteria(
                    "cho", null, "gâteau", 1, 10
            );
            System.out.println("Résultats obtenus:");
            chocolatResult.forEach(i -> System.out.println("  - " + i.getName()));

        } catch (Exception e) {
            System.err.println("\nERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}