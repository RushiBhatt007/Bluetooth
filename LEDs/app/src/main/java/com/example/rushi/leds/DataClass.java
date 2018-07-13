package com.example.rushi.leds;

import org.json.JSONException;
import org.json.JSONObject;

public class DataClass {
    private double uid;
    private double weight;
    private double distance;

    public DataClass(double uid, double weight, double distance){
        this.uid = uid;
        this.weight = weight;
        this.distance = distance;
    }

    public static DataClass getClass(String stream){
        try{
            JSONObject current = new JSONObject(stream);
            return new DataClass(current.getDouble("uid"), current.getDouble("weight"), current.getDouble("distance"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUid() {
        return Double.toString(uid);
    }

    public String getWeight() {
        return Double.toString(weight);
    }

    public String getDistance() {
        return Double.toString(distance);
    }
}
