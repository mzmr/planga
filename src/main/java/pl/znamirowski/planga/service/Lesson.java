package pl.znamirowski.planga.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.znamirowski.planga.generator.AppSettings;
import pl.znamirowski.planga.generator.GroupType;
import pl.znamirowski.planga.generator.LessonTuple;

import java.time.LocalTime;

public class Lesson {
    private final int lectureGroupId;
    private final int auditoryGroupId;
    private final int laboratoryGroupId;
    private final int teacherId;
    private final int courseId;
    private final int durationInTimeWindows;
    private final int dayNumber;
    private final int roomNumber;
    private final GroupType groupType;

    @JsonFormat(pattern="HH:mm")
    private final LocalTime startTime;

    public Lesson(LessonTuple lessonTuple, int dayNumber, int windowNumber, int roomNumber, AppSettings settings) {
        this.groupType = lessonTuple.getGroupType();
        this.lectureGroupId = lessonTuple.getLectureGroupId();
        this.auditoryGroupId = lessonTuple.getAuditoryGroupId();
        this.laboratoryGroupId = lessonTuple.getLaboratoryGroupId();
        this.teacherId = lessonTuple.getTeacherId();
        this.courseId = lessonTuple.getCourseId();
        this.durationInTimeWindows = lessonTuple.getTimeUnits();
        this.dayNumber = dayNumber;
        this.startTime = settings.getStartHour().plusMinutes(windowNumber * settings.getMinutesPerTimeWindow());
        this.roomNumber = roomNumber;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public int getLectureGroupId() {
        return lectureGroupId;
    }

    public int getAuditoryGroupId() {
        return auditoryGroupId;
    }

    public int getLaboratoryGroupId() {
        return laboratoryGroupId;
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
