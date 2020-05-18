package pl.znamirowski.planga;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

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

    private final List<LessonTuple> lessonTuples;

    public AppSettings(InputSettings inputSettings) {
        numberOfRooms = inputSettings.getNumberOfRooms();
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

        lessonTuples = createLessonTuples(inputSettings.getGroups());
    }

    private List<LessonTuple> createLessonTuples(List<Group> groups) {
        List<LessonTuple> lessonTuples = new ArrayList<>();
//        int currentId = 0;
        for (Group group : groups) {
            for (CourseSettings course : group.getCourses()) {
                LessonTuple lessonTuple = new LessonTuple();
//                lessonTuple.setId(currentId++);
                lessonTuple.setGroupId(group.getId());
                lessonTuple.setCourseId(course.getCourseId());
                lessonTuple.setTeacherId(course.getTeacherId());
                lessonTuple.setTimeUnits(course.getTimeUnits());
                lessonTuples.add(lessonTuple);
            }
        }
        return lessonTuples;
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

    public List<LessonTuple> getLessonTuples() {
        return lessonTuples;
    }
}
