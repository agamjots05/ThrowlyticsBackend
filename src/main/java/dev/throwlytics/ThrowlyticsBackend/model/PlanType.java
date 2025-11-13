package dev.throwlytics.ThrowlyticsBackend.model;

public enum PlanType {
    FREE(5);

    private final int monthlyTokens;

    PlanType(int monthlyToken){
        this.monthlyTokens = monthlyToken;
    }
    public int getMonthlyTokens() {
        return monthlyTokens;
    }
}
