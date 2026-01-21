package restaurant.database;

import restaurant.models.Dish;
import restaurant.models.Ingredient;
import restaurant.enums.CategoryEnum;
import restaurant.enums.DishTypeEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    // a) Récupérer un plat par ID avec ses ingrédients (ManyToMany)
    public Dish findDishById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID ne peut pas être null");
        }

        String dishQuery = "SELECT * FROM dish WHERE id = ?";
        String ingredientsQuery = """
            SELECT i.id, i.name, i.price, i.category, 
                   di.quantity_required, di.unit
            FROM ingredient i
            JOIN dishingredient di ON i.id = di.id_ingredient
            WHERE di.id_dish = ?
            ORDER BY i.id
            """;

        try (Connection conn = DBConnection.getDBConnection();
             PreparedStatement dishStmt = conn.prepareStatement(dishQuery)) {

            dishStmt.setInt(1, id);
            ResultSet dishRs = dishStmt.executeQuery();

            if (!dishRs.next()) {
                throw new RuntimeException("Plat non trouvé avec l'ID: " + id);
            }

            Dish dish = new Dish(
                    dishRs.getInt("id"),
                    dishRs.getString("name"),
                    DishTypeEnum.valueOf(dishRs.getString("dish_type"))
            );

            Object sellingPrice = dishRs.getObject("selling_price");
            if (sellingPrice != null) {
                dish.setSellingPrice(dishRs.getDouble("selling_price"));
            }

            try (PreparedStatement ingStmt = conn.prepareStatement(ingredientsQuery)) {
                ingStmt.setInt(1, id);
                ResultSet ingRs = ingStmt.executeQuery();

                List<Ingredient> ingredients = new ArrayList<>();
                while (ingRs.next()) {
                    Ingredient ingredient = new Ingredient(
                            ingRs.getInt("id"),
                            ingRs.getString("name"),
                            ingRs.getDouble("price"),
                            CategoryEnum.valueOf(ingRs.getString("category")),
                            id,
                            ingRs.getDouble("quantity_required")
                    );
                    ingredients.add(ingredient);
                }
                dish.setIngredients(ingredients);
            }

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du plat avec ID: " + id, e);
        }
    }

    // b) Pagination des ingrédients (adaptée)
    public List<Ingredient> findIngredients(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        int offset = (page - 1) * size;
        List<Ingredient> ingredients = new ArrayList<>();

        String query = "SELECT * FROM ingredient ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        null,
                        null
                );
                ingredients.add(ingredient);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération paginée des ingrédients", e);
        }

        return ingredients;
    }

    // c) Créer plusieurs ingrédients avec atomicité
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            throw new IllegalArgumentException("La liste d'ingrédients ne peut pas être vide");
        }

        Connection conn = null;
        List<Ingredient> createdIngredients = new ArrayList<>();

        try {
            conn = DBConnection.getDBConnection();
            conn.setAutoCommit(false);

            // 1. Vérifier qu'aucun ingrédient n'existe déjà
            String checkQuery = "SELECT id FROM ingredient WHERE LOWER(name) = LOWER(?)";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                for (Ingredient ing : newIngredients) {
                    if (ing.getName() == null || ing.getName().trim().isEmpty()) {
                        throw new IllegalArgumentException("Le nom d'un ingrédient ne peut pas être vide");
                    }

                    String ingredientName = ing.getName().trim().toLowerCase();
                    checkStmt.setString(1, ingredientName);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        throw new RuntimeException("L'ingrédient existe déjà: " + ing.getName());
                    }
                }
            }

            String insertQuery = "INSERT INTO ingredient (name, price, category) " +
                    "VALUES (?, ?, ?::category_enum) RETURNING id";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                for (Ingredient ing : newIngredients) {
                    insertStmt.setString(1, ing.getName().trim());
                    insertStmt.setDouble(2, ing.getPrice());
                    insertStmt.setString(3, ing.getCategory().name());

                    ResultSet rs = insertStmt.executeQuery();
                    if (rs.next()) {
                        ing.setId(rs.getInt(1));
                        createdIngredients.add(ing);
                    }
                }
            }

            conn.commit();
            return createdIngredients;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Erreur lors du rollback", ex);
                }
            }
            throw new RuntimeException("Erreur lors de la création des ingrédients", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ignorer
                }
            }
        }
    }

    // d) Sauvegarder un plat (insert ou update) avec ManyToMany
    public Dish saveDish(Dish dishToSave) {
        if (dishToSave == null) {
            throw new IllegalArgumentException("Le plat ne peut pas être null");
        }

        Connection conn = null;

        try {
            conn = DBConnection.getDBConnection();
            conn.setAutoCommit(false);

            System.out.println("DEBUG: Début saveDish - Plat: " + dishToSave.getName() +
                    ", ID: " + dishToSave.getId());

            boolean dishExists = false;
            if (dishToSave.getId() != null) {
                dishExists = dishExists(conn, dishToSave.getId());
                System.out.println("DEBUG: Plat existe? " + dishExists);
            }

            Dish savedDish;
            if (dishExists) {
                savedDish = updateDish(conn, dishToSave);
            } else {
                savedDish = insertDish(conn, dishToSave);
            }

            System.out.println("DEBUG: Plat sauvegardé avec ID: " + savedDish.getId());

            List<Ingredient> ingredients = dishToSave.getIngredients();
            if (ingredients == null) {
                ingredients = new ArrayList<>();
            }
            System.out.println("DEBUG: Gestion de " + ingredients.size() + " ingrédient(s)");

            manageDishIngredientsManyToMany(conn, savedDish, ingredients);

            conn.commit();
            System.out.println("DEBUG: Transaction commitée avec succès");
            return savedDish;

        } catch (SQLException e) {
            System.err.println("DEBUG: ERREUR SQL dans saveDish: " + e.getMessage());
            System.err.println("DEBUG: Cause SQL: " + e.getSQLState() + " - " + e.getErrorCode());

            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("DEBUG: Rollback effectué");
                } catch (SQLException ex) {
                    throw new RuntimeException("Erreur lors du rollback", ex);
                }
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du plat: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ignorer
                }
            }
        }
    }

    // e) Trouver les plats par nom d'ingrédient (ManyToMany)
    public List<Dish> findDishsByIngredientName(String ingredientName) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Dish> dishes = new ArrayList<>();
        String query = """
            SELECT DISTINCT d.*
            FROM dish d
            JOIN dishingredient di ON d.id = di.id_dish
            JOIN ingredient i ON di.id_ingredient = i.id
            WHERE LOWER(i.name) LIKE LOWER(?)
            ORDER BY d.id
            """;

        try (Connection conn = DBConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + ingredientName.trim() + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Dish dish = new Dish(
                        rs.getInt("id"),
                        rs.getString("name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type"))
                );

                Object sellingPrice = rs.getObject("selling_price");
                if (sellingPrice != null) {
                    dish.setSellingPrice(rs.getDouble("selling_price"));
                }

                List<Ingredient> ingredients = getIngredientsForDishManyToMany(conn, dish.getId());
                dish.setIngredients(ingredients);

                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de plats par ingrédient", e);
        }

        return dishes;
    }

    // f) Recherche d'ingrédients par critères multiples avec pagination
    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size) {

        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;

        List<Ingredient> ingredients = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT DISTINCT i.* FROM ingredient i"
        );

        boolean needDishJoin = (dishName != null && !dishName.trim().isEmpty());

        if (needDishJoin) {
            queryBuilder.append(" LEFT JOIN dishingredient di ON i.id = di.id_ingredient");
            queryBuilder.append(" LEFT JOIN dish d ON di.id_dish = d.id");
        }

        queryBuilder.append(" WHERE 1=1");

        List<Object> parameters = new ArrayList<>();

        if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            queryBuilder.append(" AND LOWER(i.name) LIKE LOWER(?)");
            parameters.add("%" + ingredientName.trim() + "%");
        }

        if (category != null) {
            queryBuilder.append(" AND i.category = ?::category_enum");
            parameters.add(category.name());
        }

        if (dishName != null && !dishName.trim().isEmpty()) {
            queryBuilder.append(" AND LOWER(d.name) LIKE LOWER(?)");
            parameters.add("%" + dishName.trim() + "%");
        }

        queryBuilder.append(" ORDER BY i.id LIMIT ? OFFSET ?");
        parameters.add(size);
        parameters.add(offset);

        try (Connection conn = DBConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        null,
                        null
                );
                ingredients.add(ingredient);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche d'ingrédients par critères", e);
        }

        return ingredients;
    }

    private boolean dishExists(Connection conn, Integer dishId) throws SQLException {
        String query = "SELECT COUNT(*) FROM dish WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private Dish insertDish(Connection conn, Dish dish) throws SQLException {
        String query = """
            INSERT INTO dish (name, dish_type, selling_price) 
            VALUES (?, ?::dish_type_enum, ?) 
            RETURNING id
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dish.getName());
            stmt.setString(2, dish.getDishType().name());

            if (dish.getSellingPrice() != null) {
                stmt.setDouble(3, dish.getSellingPrice());
            } else {
                stmt.setNull(3, Types.DOUBLE);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                dish.setId(rs.getInt(1));
            }
            return dish;
        }
    }

    private Dish updateDish(Connection conn, Dish dish) throws SQLException {
        String query = """
            UPDATE dish 
            SET name = ?, dish_type = ?::dish_type_enum, selling_price = ? 
            WHERE id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dish.getName());
            stmt.setString(2, dish.getDishType().name());

            if (dish.getSellingPrice() != null) {
                stmt.setDouble(3, dish.getSellingPrice());
            } else {
                stmt.setNull(3, Types.DOUBLE);
            }

            stmt.setInt(4, dish.getId());
            stmt.executeUpdate();
            return dish;
        }
    }

    private void manageDishIngredientsManyToMany(Connection conn, Dish dish, List<Ingredient> newIngredients) throws SQLException {
        // 1. Supprimer toutes les relations existantes pour ce plat
        String deleteQuery = "DELETE FROM dishingredient WHERE id_dish = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, dish.getId());
            stmt.executeUpdate();
        }

        // 2. Créer de nouvelles relations
        if (newIngredients != null && !newIngredients.isEmpty()) {
            String insertQuery = """
                INSERT INTO dishingredient (id_dish, id_ingredient, quantity_required, unit)
                VALUES (?, ?, ?, ?::unit_enum)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                for (Ingredient ing : newIngredients) {
                    if (ing.getId() != null && ing.getRequiredQuantity() != null) {
                        stmt.setInt(1, dish.getId());
                        stmt.setInt(2, ing.getId());
                        stmt.setDouble(3, ing.getRequiredQuantity());
                        stmt.setString(4, "KG");
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            }
        }

        // 3. Récupérer les ingrédients associés
        dish.setIngredients(getIngredientsForDishManyToMany(conn, dish.getId()));
    }

    private List<Ingredient> getIngredientsForDishManyToMany(Connection conn, Integer dishId) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = """
            SELECT i.id, i.name, i.price, i.category, 
                   di.quantity_required, di.unit
            FROM ingredient i
            JOIN dishingredient di ON i.id = di.id_ingredient
            WHERE di.id_dish = ?
            ORDER BY i.id
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        dishId,
                        rs.getDouble("quantity_required")
                );
                ingredients.add(ingredient);
            }
        }

        return ingredients;
    }

    private List<Ingredient> getIngredientsForDish(Connection conn, Integer dishId) throws SQLException {
        return getIngredientsForDishManyToMany(conn, dishId);
    }

    public int countAllIngredients() {
        String query = "SELECT COUNT(*) FROM ingredient";

        try (Connection conn = DBConnection.getDBConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des ingrédients", e);
        }
    }

    public List<Dish> findAllDishes() {
        List<Dish> dishes = new ArrayList<>();
        String query = "SELECT * FROM dish ORDER BY id";

        try (Connection conn = DBConnection.getDBConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Dish dish = new Dish(
                        rs.getInt("id"),
                        rs.getString("name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type"))
                );

                Object sellingPrice = rs.getObject("selling_price");
                if (sellingPrice != null) {
                    dish.setSellingPrice(rs.getDouble("selling_price"));
                }

                List<Ingredient> ingredients = getIngredientsForDishManyToMany(conn, dish.getId());
                dish.setIngredients(ingredients);

                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les plats", e);
        }

        return dishes;
    }

    public boolean updateRequiredQuantity(Integer ingredientId, Double requiredQuantity) {
        System.err.println("ATTENTION: updateRequiredQuantity est dépréciée avec le schéma ManyToMany");
        System.err.println("Utilisez la table dishingredient à la place");
        return false;
    }

    public List<Ingredient> findIngredientsWithoutRequiredQuantity() {
        System.err.println("ATTENTION: Cette méthode n'a plus de sens avec ManyToMany");
        return new ArrayList<>();
    }

    public Double calculateDishCost(Integer dishId) {
        Dish dish = findDishById(dishId);

        try {
            return dish.getDishCost();
        } catch (RuntimeException e) {
            throw new RuntimeException("Erreur lors du calcul du coût du plat ID " + dishId + ": " + e.getMessage());
        }
    }

    public Double getGrossMargin(Integer dishId) {
        Dish dish = findDishById(dishId);

        if (dish.getSellingPrice() == null) {
            throw new RuntimeException("Prix de vente NULL pour le plat: " + dish.getName());
        }

        try {
            Double cost = dish.getDishCost();
            return dish.getSellingPrice() - cost;
        } catch (RuntimeException e) {
            throw new RuntimeException("Erreur lors du calcul de la marge: " + e.getMessage());
        }
    }

    /**
     * Méthode utilitaire pour sauvegarder un ingrédient seul
     */
    public Ingredient saveIngredient(Ingredient ingredient) {
        if (ingredient == null) {
            throw new IllegalArgumentException("L'ingrédient ne peut pas être null");
        }

        boolean isUpdate = ingredient.getId() != null;
        String query;

        if (isUpdate) {
            query = "UPDATE ingredient SET name = ?, price = ?, category = ?::category_enum WHERE id = ? RETURNING id";
        } else {
            query = "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?::category_enum) RETURNING id";
        }

        try (Connection conn = DBConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ingredient.getName());
            stmt.setDouble(2, ingredient.getPrice());
            stmt.setString(3, ingredient.getCategory().name());

            if (isUpdate) {
                stmt.setInt(4, ingredient.getId());
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next() && !isUpdate) {
                ingredient.setId(rs.getInt(1));
            }

            return ingredient;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'ingrédient", e);
        }
    }

    public void addIngredientToDish(Integer dishId, Integer ingredientId, Double quantity, String unit) {
        String query = """
            INSERT INTO dishingredient (id_dish, id_ingredient, quantity_required, unit)
            VALUES (?, ?, ?, ?::unit_enum)
            ON CONFLICT (id_dish, id_ingredient) 
            DO UPDATE SET quantity_required = EXCLUDED.quantity_required, unit = EXCLUDED.unit
            """;

        try (Connection conn = DBConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, dishId);
            stmt.setInt(2, ingredientId);
            stmt.setDouble(3, quantity);
            stmt.setString(4, unit);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'ingrédient au plat", e);
        }
    }

    public void testCalculs() {
        System.out.println("=== TEST DES CALCULS (TD3) ===");

        Object[][] tests = {
                {1, "Salade fraîche", 250.0, 3250.0},
                {2, "Poulet grillé", 4500.0, 7500.0},
                {3, "Riz aux légumes", 0.0, null},
                {4, "Gâteau au chocolat", 1400.0, 6600.0},
                {5, "Salade de fruits", 0.0, null}
        };

        for (Object[] test : tests) {
            int dishId = (int) test[0];
            String dishName = (String) test[1];
            double expectedCost = (double) test[2];
            Double expectedMargin = (Double) test[3];

            try {
                Dish dish = findDishById(dishId);
                Double cost = dish.getDishCost();

                System.out.printf("\n%s (ID: %d):\n", dishName, dishId);
                System.out.printf("  Coût calculé: %.2f Ar", cost);

                if (Math.abs(cost - expectedCost) < 0.01) {
                    System.out.printf(" ✓ (Attendu: %.2f Ar)\n", expectedCost);
                } else {
                    System.out.printf(" ✗ (Attendu: %.2f Ar)\n", expectedCost);
                }

                try {
                    Double margin = getGrossMargin(dishId);
                    System.out.printf("  Marge brute: %.2f Ar", margin);

                    if (expectedMargin != null && Math.abs(margin - expectedMargin) < 0.01) {
                        System.out.printf(" ✓ (Attendu: %.2f Ar)\n", expectedMargin);
                    } else if (expectedMargin == null) {
                        System.out.println(" ✗ (Devrait lever une exception)");
                    } else {
                        System.out.printf(" ✗ (Attendu: %.2f Ar)\n", expectedMargin);
                    }
                } catch (RuntimeException e) {
                    if (expectedMargin == null) {
                        System.out.printf("  Marge brute: Exception ✓ (%s)\n", e.getMessage());
                    } else {
                        System.out.printf("  Marge brute: Exception ✗ (Inattendue: %s)\n", e.getMessage());
                    }
                }

            } catch (Exception e) {
                System.out.printf("\n%s: ERREUR - %s\n", dishName, e.getMessage());
            }
        }
    }
}