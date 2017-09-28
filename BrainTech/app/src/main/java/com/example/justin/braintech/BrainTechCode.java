package com.example.justin.braintech;

import java.util.ArrayList;

/**
 * Created by Justin on 9/20/2017.
 */
// This class can be used with all activities as long as a quantifiable score can be achieved.

public class BrainTechCode {
    private ArrayList<Double> recentScores; // Represents the values of the scores that the patient has received.

    private double populationParameter; // Represents the population parameter that we will be comparing against.


    public BrainTechCode() { // Constructor used to create each new BrainTech That will be used for each activity to calculate significant changes.
        recentScores = new ArrayList<Double>();
    }

    public void addScore(double d) { // When a new score is available after the patient plays an activity, adds it to the rest of the recent scores.
        recentScores.add(d);
    }

    public void setPopulationParameter(double d) { //When a significant change is picked, allows for the population parameter to be adjusted to the new value.
        populationParameter = d;
    }

    public double calcAverage() { // Calculates the average of all the recent scores.
        double total = 0;
        for(int i = 0; i < recentScores.size(); i++) {
            total = total + recentScores.get(i);
        }

        return total / recentScores.size();
    }

    public double calcStdDev() { // Calculates the standard deviation of the sample.
        double avgholder = calcAverage();
        double stddev = 0;
        for (int i = 0; i < recentScores.size(); i++) {
            stddev = stddev + (((recentScores.get(i)-avgholder) * (recentScores.get(i)-avgholder))/recentScores.size());
        }
        return Math.sqrt(stddev);
    }

    public double calcZScore() { // Calculates Z score, which will be able to indiciate significance.
        double statistic = calcAverage();
        return (statistic - populationParameter)/ (calcStdDev()/ (Math.sqrt(recentScores.size())));
    }

    public boolean statisticalSignificance() { //Returns a boolean that will indicate a significant change in the score.
        if ( calcZScore() > 1.96) {
            return true;
        }
        if (calcZScore() < -1.96) {
            return true;
        }
        return false;
    }

}
