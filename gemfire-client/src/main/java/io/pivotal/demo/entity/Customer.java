package io.pivotal.demo.entity;

import java.io.Serializable;

public class Customer implements Serializable {
    private String id;
    private String name;
    private int income;

    public Customer() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + name + "', income=" + income + "}";
    }
}
