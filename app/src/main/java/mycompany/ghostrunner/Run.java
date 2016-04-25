package mycompany.ghostrunner;

/**
 * Created by rolofzon on 2016-04-25.
 */
public class Run {
    public int runHours;
    public int runMinutes;
    public int runSeconds;
    public float runDistance;
    public String runDate;

    public Run(int hours, int minutes, int seconds, int distance, String date) {
        runHours = hours;
        runMinutes = minutes;
        runSeconds = seconds;
        runDistance = distance;
        runDate = date;
    }

    public int getHours() {
        return runHours;
    }

    public int getMinutes() {
        return runMinutes;
    }

    public int getSeconds() {
        return runSeconds;
    }

    public float getDistance() {
        return runDistance;
    }

    public String getDate() {
        return runDate;
    }
}
