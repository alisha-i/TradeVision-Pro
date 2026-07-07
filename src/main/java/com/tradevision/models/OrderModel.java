package com.tradevision.models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class OrderModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty time;
    private final SimpleStringProperty symbol;
    private final SimpleStringProperty type;
    private final SimpleDoubleProperty quantity;
    private final SimpleDoubleProperty price;

    public OrderModel(int id, String time, String symbol, String type, double quantity, double price) {
        this.id = new SimpleIntegerProperty(id);
        this.time = new SimpleStringProperty(time);
        this.symbol = new SimpleStringProperty(symbol);
        this.type = new SimpleStringProperty(type);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
    }

    public int getId() { return id.get(); }
    public String getTime() { return time.get(); }
    public String getSymbol() { return symbol.get(); }
    public String getType() { return type.get(); }
    public double getQuantity() { return quantity.get(); }
    public double getPrice() { return price.get(); }
}
