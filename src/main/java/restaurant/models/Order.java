package restaurant.models;

import restaurant.enums.PaymentStatusEnum;
import java.time.Instant;
import java.util.List;

public class Order {
    private Integer id;
    private String reference;
    private PaymentStatusEnum paymentStatus;
    private List<Dish> dishes;
    private Instant orderDate;

    // Constructeurs
    public Order() {}

    public Order(Integer id, String reference, PaymentStatusEnum paymentStatus) {
        this.id = id;
        this.reference = reference;
        this.paymentStatus = paymentStatus;
    }

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public PaymentStatusEnum getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatusEnum paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public List<Dish> getDishes() { return dishes; }
    public void setDishes(List<Dish> dishes) { this.dishes = dishes; }

    public Instant getOrderDate() { return orderDate; }
    public void setOrderDate(Instant orderDate) { this.orderDate = orderDate; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", paymentStatus=" + paymentStatus +
                ", orderDate=" + orderDate +
                '}';
    }
}
