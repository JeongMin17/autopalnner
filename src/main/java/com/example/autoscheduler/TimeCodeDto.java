package com.example.autoscheduler;
public class TimeCodeDto {
    private int person;
    private String sex;
    private String timeCode;

    public TimeCodeDto(int person, String sex, String timeCode) {
        this.person = person;
        this.sex = sex;
        this.timeCode = timeCode;
    }

    // Getter methods
    public int getPerson() {
        return person;
    }

    public String getSex() {
        return sex;
    }

    public String getTimeCode() {
        return timeCode;
    }
}