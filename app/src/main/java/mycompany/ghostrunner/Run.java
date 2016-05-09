package mycompany.ghostrunner;

import java.io.Serializable;

/**
 * Created by rolofzon on 2016-04-25.
 *
 */
public class Run implements Serializable {
    private int runHours;
    private int runMinutes;
    private int runSeconds;
    private float runDistance;
    private String runDate;
    private String runName;

    public Run(String name, int hours, int minutes, int seconds, int distance, String date) {
        runName = name;
        runHours = hours;
        runMinutes = minutes;
        runSeconds = seconds;
        runDistance = distance;
        runDate = date;
    }

    public int getHours() {
        return runHours;
    }

    public String getName() { return runName; }

    public int getMinutes() { return runMinutes; }

    public int getSeconds() {
        return runSeconds;
    }

    public float getDistance() {
        return runDistance;
    }

    public String getDate() {
        return runDate;
    }

    //Todo: Kanske bör lägga in en toString() här?
}
