package com.ibm.mydoctorapp.Models;

public class Notification {
    private String patientName;
    private String category;
    private String postID;

    public Notification() {}

    public Notification(String patientName, String category, String postID) {
        this.patientName = patientName;
        this.category = category;
        this.postID = postID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }
}
