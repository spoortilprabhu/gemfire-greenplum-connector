package io.pivotal.demo.entity;

import java.io.Serializable;

public class TestRegion implements Serializable {
    private String id;
    private String name;
    private Integer income;

    public TestRegion() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getIncome() { return income; }
    public void setIncome(Integer income) { this.income = income; }

    @Override
    public String toString() {
        return "TestRegion{id='" + id + "', name='" + name + "', income=" + income + "}";
    }

    public void setValue(String valueFromGemFire) {
    }
}
