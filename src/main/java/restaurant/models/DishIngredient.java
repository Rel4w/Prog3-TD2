package restaurant.models;

import restaurant.enums.UnitEnum;

public class DishIngredient {
    private Integer id;
    private Integer dishId;
    private Integer ingredientId;
    private Double quantityRequired;
    private UnitEnum unit;

    // Constructeurs
    public DishIngredient() {}

    public DishIngredient(Integer dishId, Integer ingredientId,
                          Double quantityRequired, UnitEnum unit) {
        this.dishId = dishId;
        this.ingredientId = ingredientId;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getDishId() { return dishId; }
    public void setDishId(Integer dishId) { this.dishId = dishId; }

    public Integer getIngredientId() { return ingredientId; }
    public void setIngredientId(Integer ingredientId) { this.ingredientId = ingredientId; }

    public Double getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(Double quantityRequired) { this.quantityRequired = quantityRequired; }

    public UnitEnum getUnit() { return unit; }
    public void setUnit(UnitEnum unit) { this.unit = unit; }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", dishId=" + dishId +
                ", ingredientId=" + ingredientId +
                ", quantityRequired=" + quantityRequired +
                ", unit=" + unit +
                '}';
    }
}
