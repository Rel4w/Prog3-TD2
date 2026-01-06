package restaurant.database;

import restaurant.models.Dish;
import restaurant.models.Ingredient;
import restaurant.enums.CategoryEnum;
import restaurant.enums.DishTypeEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    // a) Récupérer un plat par ID avec ses ingrédients
    public Dish findDishById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID ne peut pas être null");
        }

        String dishQuery = "SELECT * FROM dish WHERE id = ?";
        String ingredientsQuery = "SELECT * FROM ingredient WHERE id_dish = ? ORDER BY id";

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
                            ingRs.getInt("id_dish")
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

    // b) Pagination des ingrédients
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
                        rs.getInt("id_dish")
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

                    checkStmt.setString(1, ing.getName().trim());
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        throw new RuntimeException("L'ingrédient existe déjà: " + ing.getName());
                    }
                }
            }

            // 2. Insérer tous les ingrédients
            String insertQuery = "INSERT INTO ingredient (name, price, category, id_dish) VALUES (?, ?, ?, ?) RETURNING id";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                for (Ingredient ing : newIngredients) {
                    insertStmt.setString(1, ing.getName().trim());
                    insertStmt.setDouble(2, ing.getPrice());
                    insertStmt.setString(3, ing.getCategory().name());

                    if (ing.getDishId() != null) {
                        insertStmt.setInt(4, ing.getDishId());
                    } else {
                        insertStmt.setNull(4, Types.INTEGER);
                    }

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

    // d) Sauvegarder un plat (insert ou update)
    public Dish saveDish(Dish dishToSave) {
        if (dishToSave == null) {
            throw new IllegalArgumentException("Le plat ne peut pas être null");
        }

        Connection conn = null;

        try {
            conn = DBConnection.getDBConnection();
            conn.setAutoCommit(false);

            boolean dishExists = dishToSave.getId() != null && dishExists(conn, dishToSave.getId());

            Dish savedDish;
            if (dishExists) {
                savedDish = updateDish(conn, dishToSave);
            } else {
                savedDish = insertDish(conn, dishToSave);
            }

            manageDishIngredients(conn, savedDish, dishToSave.getIngredients());

            conn.commit();
            return savedDish;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Erreur lors du rollback", ex);
                }
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du plat", e);
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
        String query = "INSERT INTO dish (name, dish_type) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dish.getName());
            stmt.setString(2, dish.getDishType().name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                dish.setId(rs.getInt(1));
            }
            return dish;
        }
    }

    private Dish updateDish(Connection conn, Dish dish) throws SQLException {
        String query = "UPDATE dish SET name = ?, dish_type = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dish.getName());
            stmt.setString(2, dish.getDishType().name());
            stmt.setInt(3, dish.getId());
            stmt.executeUpdate();
            return dish;
        }
    }

    private void manageDishIngredients(Connection conn, Dish dish, List<Ingredient> newIngredients) throws SQLException {
        // 1. Dissocier tous les ingrédients actuels du plat
        String dissociateQuery = "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?";
        try (PreparedStatement stmt = conn.prepareStatement(dissociateQuery)) {
            stmt.setInt(1, dish.getId());
            stmt.executeUpdate();
        }

        // 2. Associer les nouveaux ingrédients
        if (newIngredients != null && !newIngredients.isEmpty()) {
            String associateQuery = "UPDATE ingredient SET id_dish = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(associateQuery)) {
                for (Ingredient ing : newIngredients) {
                    if (ing.getId() != null) {
                        stmt.setInt(1, dish.getId());
                        stmt.setInt(2, ing.getId());
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            }
        }

        // 3. Récupérer les ingrédients associés pour l'objet Dish
        String getIngredientsQuery = "SELECT * FROM ingredient WHERE id_dish = ?";
        try (PreparedStatement stmt = conn.prepareStatement(getIngredientsQuery)) {
            stmt.setInt(1, dish.getId());
            ResultSet rs = stmt.executeQuery();

            List<Ingredient> associatedIngredients = new ArrayList<>();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        rs.getInt("id_dish")
                );
                associatedIngredients.add(ingredient);
            }
            dish.setIngredients(associatedIngredients);
        }
    }

    // e) Trouver les plats par nom d'ingrédient
    public List<Dish> findDishsByIngredientName(String ingredientName) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Dish> dishes = new ArrayList<>();
        String query = """
            SELECT DISTINCT d.*
            FROM dish d
            JOIN ingredient i ON d.id = i.id_dish
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

                List<Ingredient> ingredients = getIngredientsForDish(conn, dish.getId());
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
                "SELECT i.* FROM ingredient i LEFT JOIN dish d ON i.id_dish = d.id WHERE 1=1"
        );
        List<Object> parameters = new ArrayList<>();

        if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            queryBuilder.append(" AND LOWER(i.name) LIKE LOWER(?)");
            parameters.add("%" + ingredientName.trim() + "%");
        }

        if (category != null) {
            queryBuilder.append(" AND i.category = ?");
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
                        rs.getInt("id_dish")
                );
                ingredients.add(ingredient);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche d'ingrédients par critères", e);
        }

        return ingredients;
    }

    private List<Ingredient> getIngredientsForDish(Connection conn, Integer dishId) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredient WHERE id_dish = ? ORDER BY id";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        rs.getInt("id_dish")
                );
                ingredients.add(ingredient);
            }
        }

        return ingredients;
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

                List<Ingredient> ingredients = getIngredientsForDish(conn, dish.getId());
                dish.setIngredients(ingredients);

                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les plats", e);
        }

        return dishes;
    }
}
