package restaurant;

import restaurant.database.DataRetriever;
import restaurant.models.Dish;
import restaurant.models.Ingredient;
import restaurant.enums.CategoryEnum;
import restaurant.enums.DishTypeEnum;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever();

        try {
            // TEST 1: Dish findDishById avec getDishCost
            System.out.println("1. TEST DE Dish findDishById avec getDishCost()");

            // Test a: Plat avec quantité connue
            System.out.println("\na) findDishById(1) - Salade fraîche:");
            Dish salade = retriever.findDishById(1);
            System.out.println("• Plat: " + salade.getName());
            System.out.println("• Type: " + salade.getDishType());
            System.out.println("• Ingrédients: " + salade.getIngredients().size());

            for (Ingredient ing : salade.getIngredients()) {
                System.out.println("  - " + ing.getName() +
                        " (Prix: " + ing.getPrice() +
                        ", Quantité: " + ing.getRequiredQuantity() + ")");
            }

            try {
                double cout = salade.getDishCost();
                System.out.println("\n• getDishCost() = " + cout + " Ar");
                System.out.println("COMPORTEMENT ATTENDU: Coût calculé ");
            } catch (Exception e) {
                System.out.println("\n• Exception: " + e.getMessage());
                System.out.println("COMPORTEMENT INATTENDU!");
            }

            // Test b: Plat avec quantité inconnue
            System.out.println("\n\nb) findDishById(4) - Gâteau au chocolat:");
            Dish gateau = retriever.findDishById(4);
            System.out.println("• Plat: " + gateau.getName());
            System.out.println("• Ingrédients: " + gateau.getIngredients().size());

            for (Ingredient ing : gateau.getIngredients()) {
                System.out.println("  - " + ing.getName() +
                        " (Prix: " + ing.getPrice() +
                        ", Quantité: " + (ing.getRequiredQuantity() != null ? ing.getRequiredQuantity() : "NULL") + ")");
            }

            try {
                double cout = gateau.getDishCost();
                System.out.println("\n• getDishCost() = " + cout + " Ar");
                System.out.println("COMPORTEMENT INATTENDU: Devrait lever une exception!");
            } catch (Exception e) {
                System.out.println("\n• Exception levée: " + e.getMessage());
                System.out.println("COMPORTEMENT ATTENDU: Exception quand quantité inconnue ");
            }

            // TEST 2: Dish saveDish - Création nouveau plat
            System.out.println("\n\n2. TEST DE Dish saveDish - Création");

            List<Ingredient> ingredientsExistants = retriever.findIngredients(1, 10);
            if (!ingredientsExistants.isEmpty()) {
                Ingredient ingredientExistant = ingredientsExistants.get(0);
                System.out.println("• Ingrédient existant utilisé: " + ingredientExistant.getName());

                Dish soupe = new Dish();
                soupe.setName("Soupe de légumes");
                soupe.setDishType(DishTypeEnum.START);

                List<Ingredient> ingredientsSoupe = new ArrayList<>();
                ingredientsSoupe.add(ingredientExistant);
                soupe.setIngredients(ingredientsSoupe);

                try {
                    Dish soupeSauvegardee = retriever.saveDish(soupe);
                    System.out.println("• Plat créé avec ID: " + soupeSauvegardee.getId());
                    System.out.println("COMPORTEMENT ATTENDU: Plat créé ");

                    try {
                        double coutSoupe = soupeSauvegardee.getDishCost();
                        System.out.println("• Coût du nouveau plat: " + coutSoupe + " Ar");
                    } catch (Exception e) {
                        System.out.println("• Coût non calculable: " + e.getMessage());
                    }

                } catch (Exception e) {
                    System.out.println("• ERREUR création plat: " + e.getMessage());
                    System.out.println("Problème dans saveDish()");
                }
            }

            // TEST 3: Dish saveDish - Mise à jour plat existant
            System.out.println("\n\n3. TEST DE Dish saveDish - Maj");

            System.out.println("\na) Mise à jour Salade fraîche (ID 1):");

            Dish saladeAModifier = retriever.findDishById(1);
            System.out.println("• Plat avant: " + saladeAModifier.getName());
            System.out.println("• Ingrédients avant: " + saladeAModifier.getIngredients().size());

            String ancienNom = saladeAModifier.getName();
            saladeAModifier.setName("Salade fraîche MODIFIÉE");

            try {
                Dish saladeModifiee = retriever.saveDish(saladeAModifier);
                System.out.println("• Plat après: " + saladeModifiee.getName());
                System.out.println("• Ingrédients après: " + saladeModifiee.getIngredients().size());
                System.out.println("COMPORTEMENT ATTENDU: Plat mis à jour ");

                try {
                    double cout = saladeModifiee.getDishCost();
                    System.out.println("• Coût après modification: " + cout + " Ar");
                } catch (Exception e) {
                    System.out.println("• Coût après modification: " + e.getMessage());
                }

                saladeModifiee.setName(ancienNom);
                retriever.saveDish(saladeModifiee);

            } catch (Exception e) {
                System.out.println("• ERREUR mise à jour: " + e.getMessage());
            }

            // TEST 4: Modification majeure (garder seulement fromage)
            System.out.println("\n\n4. TEST DE MODIFICATION MAJEURE");

            System.out.println(" Garder seulement fromage dans Salade fraîche:");

            boolean fromageExiste = false;
            for (Ingredient ing : ingredientsExistants) {
                if (ing.getName().toLowerCase().contains("fromage")) {
                    fromageExiste = true;
                    break;
                }
            }

            if (fromageExiste) {
                System.out.println("• Fromage existe dans la base");

                Dish saladeFinale = retriever.findDishById(1);
                saladeFinale.setName("Salade de fromage");

                List<Ingredient> seulementFromage = new ArrayList<>();
                for (Ingredient ing : saladeFinale.getIngredients()) {
                    if (ing.getName().toLowerCase().contains("fromage")) {
                        seulementFromage.add(ing);
                    }
                }

                saladeFinale.setIngredients(seulementFromage);

                try {
                    Dish resultatFinal = retriever.saveDish(saladeFinale);
                    System.out.println("• Plat modifié: " + resultatFinal.getName());
                    System.out.println("• Ingrédients restants: " + resultatFinal.getIngredients().size());
                    System.out.println(" COMPORTEMENT ATTENDU: Modification réussie ");

                } catch (Exception e) {
                    System.out.println("• ERREUR modification: " + e.getMessage());
                }
            } else {
                System.out.println("• Fromage n'existe pas dans la base - test ignoré");
            }

        } catch (Exception e) {
            System.err.println("\n ERREUR GÉNÉRALE DURANT LES TESTS:");
            System.err.println(e.getMessage());
        }
    }
}