package restaurant.models;

import restaurant.enums.UnitEnum;
import restaurant.enums.MovementTypeEnum;
import java.time.Instant;

public class StockMovement {
    private Integer id;
    private Integer ingredientId;
    private Double quantity;
    private UnitEnum unit;
    private MovementTypeEnum type;
    private Instant creationDatetime;

    // Constructeurs
    public StockMovement() {}

    public StockMovement(Integer ingredientId, Double quantity, UnitEnum unit,
                         MovementTypeEnum type, Instant creationDatetime) {
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
        this.type = type;
        this.creationDatetime = creationDatetime;
    }

    public StockMovement(Integer id, Integer ingredientId, Double quantity, UnitEnum unit,
                         MovementTypeEnum type, Instant creationDatetime) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
        this.type = type;
        this.creationDatetime = creationDatetime;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIngredientId() { return ingredientId; }
    public void setIngredientId(Integer ingredientId) { this.ingredientId = ingredientId; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public UnitEnum getUnit() { return unit; }
    public void setUnit(UnitEnum unit) { this.unit = unit; }

    public MovementTypeEnum getType() { return type; }
    public void setType(MovementTypeEnum type) { this.type = type; }

    public Instant getCreationDatetime() { return creationDatetime; }
    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", ingredientId=" + ingredientId +
                ", quantity=" + quantity +
                ", unit=" + unit +
                ", type=" + type +
                ", creationDatetime=" + creationDatetime +
                '}';
    }
}
