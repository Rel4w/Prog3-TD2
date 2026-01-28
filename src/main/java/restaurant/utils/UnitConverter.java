package restaurant.utils;

import restaurant.enums.UnitEnum;

import java.util.Map;
import java.util.HashMap;

public class UnitConverter {

    private static final Map<String, Map<UnitEnum, Map<UnitEnum, Double>>> CONVERSION_TABLE = new HashMap<>();

    static {
        // Initialisation de la table de conversion
        // Tomate
        Map<UnitEnum, Map<UnitEnum, Double>> tomateConversions = new HashMap<>();
        Map<UnitEnum, Double> tomateTo = new HashMap<>();
        tomateTo.put(UnitEnum.KG, 1.0);
        tomateTo.put(UnitEnum.PIECE, 10.0);
        tomateConversions.put(UnitEnum.KG, tomateTo);

        Map<UnitEnum, Double> tomateFromPiece = new HashMap<>();
        tomateFromPiece.put(UnitEnum.KG, 0.1); // 1 pièce = 0.1 KG
        tomateConversions.put(UnitEnum.PIECE, tomateFromPiece);

        CONVERSION_TABLE.put("Tomate", tomateConversions);

        // Laitue
        Map<UnitEnum, Map<UnitEnum, Double>> laitueConversions = new HashMap<>();
        Map<UnitEnum, Double> laitueTo = new HashMap<>();
        laitueTo.put(UnitEnum.KG, 1.0);
        laitueTo.put(UnitEnum.PIECE, 2.0);
        laitueConversions.put(UnitEnum.KG, laitueTo);

        Map<UnitEnum, Double> laitueFromPiece = new HashMap<>();
        laitueFromPiece.put(UnitEnum.KG, 0.5); // 1 pièce = 0.5 KG
        laitueConversions.put(UnitEnum.PIECE, laitueFromPiece);

        CONVERSION_TABLE.put("Laitue", laitueConversions);

        // Chocolat
        Map<UnitEnum, Map<UnitEnum, Double>> chocolatConversions = new HashMap<>();
        Map<UnitEnum, Double> chocolatTo = new HashMap<>();
        chocolatTo.put(UnitEnum.KG, 1.0);
        chocolatTo.put(UnitEnum.PIECE, 10.0);
        chocolatTo.put(UnitEnum.L, 2.5);
        chocolatConversions.put(UnitEnum.KG, chocolatTo);

        Map<UnitEnum, Double> chocolatFromPiece = new HashMap<>();
        chocolatFromPiece.put(UnitEnum.KG, 0.1); // 1 pièce = 0.1 KG
        chocolatFromPiece.put(UnitEnum.L, 0.25); // 1 pièce = 0.25 L (car 1 KG = 2.5 L)
        chocolatConversions.put(UnitEnum.PIECE, chocolatFromPiece);

        Map<UnitEnum, Double> chocolatFromL = new HashMap<>();
        chocolatFromL.put(UnitEnum.KG, 0.4); // 1 L = 0.4 KG
        chocolatFromL.put(UnitEnum.PIECE, 4.0); // 1 L = 4 pièces
        chocolatConversions.put(UnitEnum.L, chocolatFromL);

        CONVERSION_TABLE.put("Chocolat", chocolatConversions);

        // Poulet
        Map<UnitEnum, Map<UnitEnum, Double>> pouletConversions = new HashMap<>();
        Map<UnitEnum, Double> pouletTo = new HashMap<>();
        pouletTo.put(UnitEnum.KG, 1.0);
        pouletTo.put(UnitEnum.PIECE, 8.0);
        pouletConversions.put(UnitEnum.KG, pouletTo);

        Map<UnitEnum, Double> pouletFromPiece = new HashMap<>();
        pouletFromPiece.put(UnitEnum.KG, 0.125); // 1 pièce = 0.125 KG
        pouletConversions.put(UnitEnum.PIECE, pouletFromPiece);

        CONVERSION_TABLE.put("Poulet", pouletConversions);

        // Beurre
        Map<UnitEnum, Map<UnitEnum, Double>> beurreConversions = new HashMap<>();
        Map<UnitEnum, Double> beurreTo = new HashMap<>();
        beurreTo.put(UnitEnum.KG, 1.0);
        beurreTo.put(UnitEnum.PIECE, 4.0);
        beurreTo.put(UnitEnum.L, 5.0);
        beurreConversions.put(UnitEnum.KG, beurreTo);

        Map<UnitEnum, Double> beurreFromPiece = new HashMap<>();
        beurreFromPiece.put(UnitEnum.KG, 0.25); // 1 pièce = 0.25 KG
        beurreFromPiece.put(UnitEnum.L, 1.25); // 1 pièce = 1.25 L
        beurreConversions.put(UnitEnum.PIECE, beurreFromPiece);

        Map<UnitEnum, Double> beurreFromL = new HashMap<>();
        beurreFromL.put(UnitEnum.KG, 0.2); // 1 L = 0.2 KG
        beurreFromL.put(UnitEnum.PIECE, 0.8); // 1 L = 0.8 pièces
        beurreConversions.put(UnitEnum.L, beurreFromL);

        CONVERSION_TABLE.put("Beurre", beurreConversions);
    }

    /**
     * Convertit une quantité d'une unité à une autre pour un ingrédient donné
     * @param ingredientName Nom de l'ingrédient
     * @param quantity Quantité à convertir
     * @param fromUnit Unité source
     * @param toUnit Unité cible
     * @return Quantité convertie, ou null si la conversion n'est pas possible
     */
    public static Double convert(String ingredientName, Double quantity,
                                 UnitEnum fromUnit, UnitEnum toUnit) {
        if (fromUnit == toUnit) {
            return quantity;
        }

        Map<UnitEnum, Map<UnitEnum, Double>> ingredientConversions =
                CONVERSION_TABLE.get(ingredientName);

        if (ingredientConversions == null) {
            return null; // Pas de conversion définie pour cet ingrédient
        }

        Map<UnitEnum, Double> fromConversions = ingredientConversions.get(fromUnit);
        if (fromConversions == null) {
            return null; // Pas de conversion depuis cette unité
        }

        Double conversionRate = fromConversions.get(toUnit);
        if (conversionRate == null) {
            return null; // Conversion spécifique non disponible
        }

        return quantity * conversionRate;
    }

    /**
     * Convertit en KG (unité de base pour les stocks)
     */
    public static Double convertToKg(String ingredientName, Double quantity, UnitEnum unit) {
        if (unit == UnitEnum.KG) {
            return quantity;
        }
        return convert(ingredientName, quantity, unit, UnitEnum.KG);
    }

    /**
     * Vérifie si une conversion est possible
     */
    public static boolean canConvert(String ingredientName, UnitEnum fromUnit, UnitEnum toUnit) {
        if (fromUnit == toUnit) return true;

        Map<UnitEnum, Map<UnitEnum, Double>> ingredientConversions =
                CONVERSION_TABLE.get(ingredientName);

        if (ingredientConversions == null) return false;

        Map<UnitEnum, Double> fromConversions = ingredientConversions.get(fromUnit);
        if (fromConversions == null) return false;

        return fromConversions.containsKey(toUnit);
    }
}
