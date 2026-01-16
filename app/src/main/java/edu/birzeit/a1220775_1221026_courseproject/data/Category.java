package edu.birzeit.a1220775_1221026_courseproject.data;

public class Category {
    private int id;
    private String userEmail;
    private String type; // "INCOME" or "EXPENSE"
    private String name;
    private String color;
    private String icon;

    public Category() {
    }

    public Category(String userEmail, String type, String name, String color, String icon) {
        this.userEmail = userEmail;
        this.type = type;
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
