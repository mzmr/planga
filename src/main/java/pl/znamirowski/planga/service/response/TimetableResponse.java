package pl.znamirowski.planga.service.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.znamirowski.planga.generator.settings.AppSettings;

import java.time.LocalTime;
import java.util.List;

public class TimetableResponse {
    @JsonFormat(pattern="HH:mm")
    private final LocalTime timetableStartHour;
    @JsonFormat(pattern="HH:mm")
    private final LocalTime timetableEndHour;
    private final int timeWindowDurationInMinutes;
    private final int numberOfDaysInWeek;
    private final List<Lesson> lessons;

    public TimetableResponse(AppSettings settings, List<Lesson> lessons) {
        this.timetableStartHour = settings.getStartHour();
        this.timetableEndHour = settings.getEndHour();
        this.timeWindowDurationInMinutes = settings.getMinutesPerTimeWindow();
        this.numberOfDaysInWeek = settings.getDaysPerWeek();
        this.lessons = lessons;
    }

    public LocalTime getTimetableStartHour() {
        return timetableStartHour;
    }

    public LocalTime getTimetableEndHour() {
        return timetableEndHour;
    }

    public int getTimeWindowDurationInMinutes() {
        return timeWindowDurationInMinutes;
    }

    public int getNumberOfDaysInWeek() {
        return numberOfDaysInWeek;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }
}
