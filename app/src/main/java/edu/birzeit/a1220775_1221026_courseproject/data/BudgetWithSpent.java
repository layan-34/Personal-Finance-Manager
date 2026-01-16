package edu.birzeit.a1220775_1221026_courseproject.data;

public class BudgetWithSpent {
    private Budget budget;
    private double spentAmount;
    private String categoryName;

    public BudgetWithSpent(Budget budget, double spentAmount, String categoryName) {
        this.budget = budget;
        this.spentAmount = spentAmount;
        this.categoryName = categoryName;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
