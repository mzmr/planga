package pl.znamirowski.planga.generator.settings;

import pl.znamirowski.planga.generator.settings.input.InputSettings;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;
import static pl.znamirowski.planga.generator.settings.GroupType.AUDITORY;
import static pl.znamirowski.planga.generator.settings.GroupType.LABORATORY;
import static pl.znamirowski.planga.generator.settings.GroupType.LECTURE;

public class AppSettings {
    private final int numberOfRooms;
    private final LocalTime startHour;
    private final LocalTime endHour;
    private final LocalTime courseTimeUnit;
    private final List<Group> groups;
    private final List<Teacher> teachers;
    private final List<Course> courses;

    private final int timeWindowsPerWeek;
    private final int timeWindowsPerDay;
    private final int daysPerWeek;
    private final int minutesPerTimeWindow;
    private final int minutesPerDay;

    private Map<Integer, LessonTuple> lessonTuples;
    private List<GroupTuple> groupTuples;

    public AppSettings(InputSettings inputSettings) {
        numberOfRooms = inputSettings.getRooms().size();
        startHour = inputSettings.getStartHour();
        endHour = inputSettings.getEndHour();
        courseTimeUnit = inputSettings.getCourseTimeUnit();
        groups = inputSettings.getGroups();
        teachers = inputSettings.getTeachers();
        courses = inputSettings.getCourses();

        minutesPerDay = (int) MINUTES.between(inputSettings.getStartHour(), inputSettings.getEndHour());
        minutesPerTimeWindow = (int) MINUTES.between(LocalTime.parse("00:00"), inputSettings.getTimeStep());
        timeWindowsPerDay = minutesPerDay / minutesPerTimeWindow;
        timeWindowsPerWeek = timeWindowsPerDay * inputSettings.getDaysInWeek();
        daysPerWeek = inputSettings.getDaysInWeek();

        createLessonAndGroupTuples(inputSettings.getGroups());
    }

    private void createLessonAndGroupTuples(List<Group> groups) {
        Map<Integer, LessonTuple> lessonTuples = new HashMap<>();
        List<GroupTuple> groupTuples = new ArrayList<>();
        int tupleId = 5;
        for (Group group : groups) {
            GroupTuple lectureGroupTuple = new GroupTuple(LECTURE, group.getId(), -1, -1);
            groupTuples.add(lectureGroupTuple);
            for (CourseSettings lecture : group.getLectures()) {
                lessonTuples.put(tupleId, new LessonTuple(tupleId, lectureGroupTuple, lecture.getCourseId(),
                        lecture.getTeacherId(), lecture.getTimeUnits()));
                tupleId++;
            }
            for (int auditoryGroupId = 0; auditoryGroupId < group.getAuditoryGroupsPerLectureGroup(); auditoryGroupId++) {
                GroupTuple auditoryGroupTuple = new GroupTuple(AUDITORY, group.getId(), auditoryGroupId, -1);
                groupTuples.add(auditoryGroupTuple);
                for (CourseSettings auditory : group.getAuditoryClasses()) {
                    lessonTuples.put(tupleId, new LessonTuple(tupleId, auditoryGroupTuple, auditory.getCourseId(),
                            auditory.getTeacherId(), auditory.getTimeUnits()));
                    tupleId++;
                }
                for (int laboratoryGroupId = 0; laboratoryGroupId < group.getLaboratoryGroupsPerAuditoryGroup(); laboratoryGroupId++) {
                    GroupTuple laboratoryGroupTuple = new GroupTuple(LABORATORY, group.getId(), auditoryGroupId, laboratoryGroupId);
                    groupTuples.add(laboratoryGroupTuple);
                    for (CourseSettings laboratory : group.getLaboratoryClasses()) {
                        lessonTuples.put(tupleId, new LessonTuple(tupleId, laboratoryGroupTuple,
                                laboratory.getCourseId(), laboratory.getTeacherId(), laboratory.getTimeUnits()));
                        tupleId++;
                    }
                }
            }

        }
        this.lessonTuples = lessonTuples;
        this.groupTuples = groupTuples;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public LocalTime getStartHour() {
        return startHour;
    }

    public LocalTime getEndHour() {
        return endHour;
    }

    public LocalTime getCourseTimeUnit() {
        return courseTimeUnit;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public List<Course> getCourses() {
        return courses;
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

    public Map<Integer, LessonTuple> getLessonTuples() {
        return lessonTuples;
    }

    public List<GroupTuple> getGroupTuples() {
        return groupTuples;
    }
}
