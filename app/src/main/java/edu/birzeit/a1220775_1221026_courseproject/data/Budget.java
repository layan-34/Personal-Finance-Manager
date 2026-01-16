package edu.birzeit.a1220775_1221026_courseproject.data;

public class Budget {
    private int id;
    private String userEmail;
    private int categoryId;
    private int month;
    private double limitAmount;
    private double alertRatio; // Default 0.5

    public Budget() {
    }

    public Budget(String userEmail, int categoryId, int month, double limitAmount, double alertRatio) {
        this.userEmail = userEmail;
        this.categoryId = categoryId;
        this.month = month;
        this.limitAmount = limitAmount;
        this.alertRatio = alertRatio;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public double getAlertRatio() {
        return alertRatio;
    }

    public void setAlertRatio(double alertRatio) {
        this.alertRatio = alertRatio;
    }
}
