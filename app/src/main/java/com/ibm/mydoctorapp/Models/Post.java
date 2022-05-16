package com.ibm.mydoctorapp.Models;

import com.google.firebase.database.ServerValue;

public class Post {
    private String postKey;
    private String medicalCat;
    private String question;
    private String picture;
    private String userId;
    private Object timeStamp ;

    // make sure to have an empty constructor inside ur model class
    public Post() { }

    public Post(String medicalCat, String question, String picture, String userId) {
        this.medicalCat = medicalCat;
        this.question = question;
        this.picture = picture;
        this.userId = userId;
        this.timeStamp = ServerValue.TIMESTAMP;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getMedicalCat() {
        return medicalCat;
    }

    public void setMedicalCat(String medicalCat) {
        this.medicalCat = medicalCat;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }
}
