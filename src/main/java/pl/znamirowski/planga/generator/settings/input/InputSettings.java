package pl.znamirowski.planga.generator.settings.input;

import pl.znamirowski.planga.generator.settings.Course;
import pl.znamirowski.planga.generator.settings.Group;
import pl.znamirowski.planga.generator.settings.Room;
import pl.znamirowski.planga.generator.settings.Teacher;

import java.time.LocalTime;
import java.util.List;

public class InputSettings {
    private LocalTime startHour;
    private LocalTime endHour;
    private LocalTime timeStep;
    private LocalTime courseTimeUnit;
    private int daysInWeek;
    private List<Group> groups;
    private List<Teacher> teachers;
    private List<Course> courses;
    private List<Room> rooms;

    public LocalTime getStartHour() {
        return startHour;
    }

    public void setStartHour(LocalTime startHour) {
        this.startHour = startHour;
    }

    public LocalTime getEndHour() {
        return endHour;
    }

    public void setEndHour(LocalTime endHour) {
        this.endHour = endHour;
    }

    public LocalTime getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(LocalTime timeStep) {
        this.timeStep = timeStep;
    }

    public LocalTime getCourseTimeUnit() {
        return courseTimeUnit;
    }

    public void setCourseTimeUnit(LocalTime courseTimeUnit) {
        this.courseTimeUnit = courseTimeUnit;
    }

    public int getDaysInWeek() {
        return daysInWeek;
    }

    public void setDaysInWeek(int daysInWeek) {
        this.daysInWeek = daysInWeek;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
