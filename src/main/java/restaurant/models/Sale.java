package restaurant.models;

import java.time.Instant;

public class Sale {
    private Integer id;
    private Integer orderId;
    private Instant saleDatetime;

    // Constructeurs
    public Sale() {}

    public Sale(Integer id, Integer orderId, Instant saleDatetime) {
        this.id = id;
        this.orderId = orderId;
        this.saleDatetime = saleDatetime;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Instant getSaleDatetime() { return saleDatetime; }
    public void setSaleDatetime(Instant saleDatetime) { this.saleDatetime = saleDatetime; }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", saleDatetime=" + saleDatetime +
                '}';
    }
}