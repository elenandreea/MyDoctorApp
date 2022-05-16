package com.ibm.mydoctorapp.Models;

public class UserProfile {
    private String name;
    private String email;
    private String department;
    private String workplace;
    private String uid;

    public UserProfile() {}

    public UserProfile(String name, String email, String department, String workplace, String uid) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.workplace = workplace;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
