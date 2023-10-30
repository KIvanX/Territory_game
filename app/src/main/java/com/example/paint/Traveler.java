package com.example.paint;

public class Traveler {
    float x, y, ang;
    int user;
    boolean finished;
    Sity to_sity;

    public Traveler(float x, float y, float ang, int user, Sity to_sity) {
        this.x = x;
        this.y = y;
        this.ang = ang;
        this.user = user;
        this.to_sity = to_sity;
        this.finished = false;
    }
}
