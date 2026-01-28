package restaurant.models;

import restaurant.enums.CategoryEnum;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private Integer dishId;
    private Double requiredQuantity;
    private List<StockMovement> stockMovementList;

    // Constructeurs
    public Ingredient() {
        this.stockMovementList = new ArrayList<>();
    }

    public Ingredient(String name, Double price, CategoryEnum category, Double requiredQuantity) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.requiredQuantity = requiredQuantity;
        this.stockMovementList = new ArrayList<>();
    }

    public Ingredient(Integer id, String name, Double price, CategoryEnum category,
                      Integer dishId, Double requiredQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dishId = dishId;
        this.requiredQuantity = requiredQuantity;
        this.stockMovementList = new ArrayList<>();
    }

    public Ingredient(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dishId = null;
        this.requiredQuantity = null;
        this.stockMovementList = new ArrayList<>();
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public Double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    // Getter et Setter stockMovementList
    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        if (stockMovementList == null) {
            this.stockMovementList = new ArrayList<>();
        } else {
            this.stockMovementList = stockMovementList;
        }
    }

    public void addStockMovement(StockMovement movement) {
        if (movement != null) {
            if (this.stockMovementList == null) {
                this.stockMovementList = new ArrayList<>();
            }
            this.stockMovementList.add(movement);
        }
    }

    public Double calculateCurrentStock() {
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            return 0.0;
        }

        double stock = 0.0;
        for (StockMovement movement : stockMovementList) {
            if (movement.getType() == restaurant.enums.MovementTypeEnum.IN) {
                stock += movement.getQuantity();
            } else if (movement.getType() == restaurant.enums.MovementTypeEnum.OUT) {
                stock -= movement.getQuantity();
            }
        }
        return stock;
    }

    public boolean hasSufficientStock(Double requiredQuantity) {
        if (requiredQuantity == null || requiredQuantity <= 0) {
            return true;
        }

        Double currentStock = calculateCurrentStock();
        return currentStock >= requiredQuantity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ingredient{")
                .append("id=").append(id)
                .append(", name='").append(name).append('\'')
                .append(", price=").append(price)
                .append(", category=").append(category)
                .append(", dishId=").append(dishId)
                .append(", requiredQuantity=").append(requiredQuantity);

        if (stockMovementList != null) {
            sb.append(", stockMovements=").append(stockMovementList.size())
                    .append(", currentStock=").append(String.format("%.2f", calculateCurrentStock()));
        } else {
            sb.append(", stockMovements=0, currentStock=0.00");
        }

        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ingredient that = (Ingredient) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public Ingredient copy() {
        Ingredient copy = new Ingredient();
        copy.setId(this.id);
        copy.setName(this.name);
        copy.setPrice(this.price);
        copy.setCategory(this.category);
        copy.setDishId(this.dishId);
        copy.setRequiredQuantity(this.requiredQuantity);

        if (this.stockMovementList != null) {
            List<StockMovement> movementsCopy = new ArrayList<>();
            for (StockMovement movement : this.stockMovementList) {
                movementsCopy.add(new StockMovement(
                        movement.getId(),
                        movement.getIngredientId(),
                        movement.getQuantity(),
                        movement.getUnit(),
                        movement.getType(),
                        movement.getCreationDatetime()
                ));
            }
            copy.setStockMovementList(movementsCopy);
        }

        return copy;
    }

    public String toDisplayString() {
        return String.format("%s (%.2f Ar/kg)", name, price);
    }

    public Double getCostForQuantity(Double quantity) {
        if (quantity == null || price == null) {
            return 0.0;
        }
        return price * quantity;
    }
}