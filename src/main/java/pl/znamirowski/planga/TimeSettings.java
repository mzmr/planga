package pl.znamirowski.planga;

import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MINUTES;

public class TimeSettings {
    private final int timeWindowsPerWeek;
    private final int timeWindowsPerDay;
    private final int daysPerWeek;
    private final int minutesPerTimeWindow;
    private final int minutesPerDay;

    public TimeSettings(Settings settings) {
        minutesPerDay = (int) MINUTES.between(settings.getStartHour(), settings.getEndHour());
        minutesPerTimeWindow = (int) MINUTES.between(LocalTime.parse("00:00"), settings.getTimeStep());
        timeWindowsPerDay = (int) (minutesPerDay / minutesPerTimeWindow);
        timeWindowsPerWeek = timeWindowsPerDay * settings.getDaysInWeek();
        daysPerWeek = settings.getDaysInWeek();
    }

    public int getTimeWindowsPerWeek() {
        return timeWindowsPerWeek;
    }

    public int getTimeWindowsPerDay() {
        return timeWindowsPerDay;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public int getMinutesPerTimeWindow() {
        return minutesPerTimeWindow;
    }

    public int getMinutesPerDay() {
        return minutesPerDay;
    }
}
