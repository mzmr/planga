package pl.znamirowski.planga.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.znamirowski.planga.generator.AppSettings;
import pl.znamirowski.planga.generator.LessonTuple;

import java.time.LocalTime;

public class Lesson {
    private final int groupId;
    private final int teacherId;
    private final int courseId;
    private final int durationInTimeWindows;
    private final int dayNumber;
    private final int roomNumber;

    @JsonFormat(pattern="HH:mm")
    private final LocalTime startTime;

    public Lesson(LessonTuple lessonTuple, int dayNumber, int windowNumber, int roomNumber, AppSettings settings) {
        this.groupId = lessonTuple.getGroupId();
        this.teacherId = lessonTuple.getTeacherId();
        this.courseId = lessonTuple.getCourseId();
        this.durationInTimeWindows = lessonTuple.getTimeUnits();
        this.dayNumber = dayNumber;
        this.startTime = settings.getStartHour().plusMinutes(windowNumber * settings.getMinutesPerTimeWindow());
        this.roomNumber = roomNumber;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public int getCourseId() {
        return courseId;
    }

    public int getDurationInTimeWindows() {
        return durationInTimeWindows;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getRoomNumber() {
        return roomNumber;
    }
}
