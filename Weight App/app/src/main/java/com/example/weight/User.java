package com.example.weight;

public class User {
    private static User instance;
    // Define variables
    private String username;
    private String phone;
    private String goal = "skip";  // Set the default goal as "skip"
    private User() {
    }

    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    // Getters and setters for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getters and setters for goal
    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    // getters and setters for phone
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {

        if (phone != null && !phone.isEmpty()) {
            this.phone = phone;
        }
    }
}
