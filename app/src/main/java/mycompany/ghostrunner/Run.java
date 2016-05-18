package mycompany.ghostrunner;

import java.io.Serializable;

/**
 * Created by rolofzon on 2016-04-25.
 *
 */
public class Run implements Serializable {
    private float runHours;
    private float runMinutes;
    private float runSeconds;
    private float runDistance;
    private String runDate;
    private String runName;

    public Run(String name, float hours, float minutes, float seconds, float distance, String date) {
        runName = name;
        runHours = hours;
        runMinutes = minutes;
        runSeconds = seconds;
        runDistance = distance;
        runDate = date;
    }

    public float getHours() {
        return runHours;
    }

    public String getName() { return runName; }

    public float getMinutes() { return runMinutes; }

    public float getSeconds() {
        return runSeconds;
    }

    /**
     * Returns distance in meters.
     */
    public float getDistance() {
        return runDistance;
    }

    public String getDate() {
        return runDate;
    }

    //Todo: Kanske bör lägga in en toString() här?
}
