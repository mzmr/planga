package pl.znamirowski.planga.generator;

public class CourseSettings {
    private int courseId;
    private int teacherId;
    private int timeUnits;

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getTimeUnits() {
        return timeUnits;
    }

    public void setTimeUnits(int timeUnits) {
        this.timeUnits = timeUnits;
    }
}
