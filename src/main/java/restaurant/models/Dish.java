package restaurant.models;

import restaurant.enums.DishTypeEnum;

import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;

    // Constructeurs
    public Dish() {
        this.ingredients = new ArrayList<>();
    }

    public Dish(Integer id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = new ArrayList<>();
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Dish(String name, DishTypeEnum dishType) {
        this.name = name;
        this.dishType = dishType;
        this.ingredients = new ArrayList<>();
    }

    public Double getDishCost() {
        if (ingredients == null || ingredients.isEmpty()) {
            return 0.0;
        }

        double totalCost = 0.0;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getRequiredQuantity() == null) {
                throw new RuntimeException("Quantité nécessaire inconnue pour l'ingrédient: " +
                        ingredient.getName() + " dans le plat: " + this.name);
            }
            totalCost += ingredient.getPrice() * ingredient.getRequiredQuantity();
        }
        return totalCost;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                '}';
    }
}
