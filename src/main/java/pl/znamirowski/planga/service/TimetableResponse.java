package pl.znamirowski.planga.service;

import java.time.LocalTime;
import java.util.List;

public class TimetableResponse {
    private final LocalTime timetableStartHour;
    private final LocalTime timetableEndHour;
    private final List<Lesson> lessons;

    public TimetableResponse(LocalTime timetableStartHour, LocalTime timetableEndHour, List<Lesson> lessons) {
        this.timetableStartHour = timetableStartHour;
        this.timetableEndHour = timetableEndHour;
        this.lessons = lessons;
    }

    public LocalTime getTimetableStartHour() {
        return timetableStartHour;
    }

    public LocalTime getTimetableEndHour() {
        return timetableEndHour;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }
}
