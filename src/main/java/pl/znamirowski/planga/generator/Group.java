package pl.znamirowski.planga.generator;

import java.util.List;

public class Group {
    private int id;
    private String name;
    private List<CourseSettings> courses;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CourseSettings> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseSettings> courses) {
        this.courses = courses;
    }
}
