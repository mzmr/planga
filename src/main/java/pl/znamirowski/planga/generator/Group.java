package pl.znamirowski.planga.generator;

import java.util.List;

public class Group {
    private int id;
    private String name;
    private int auditoryGroupsPerLectureGroup;
    private int laboratoryGroupsPerAuditoryGroup;
    private List<CourseSettings> lectures;
    private List<CourseSettings> auditoryClasses;
    private List<CourseSettings> laboratoryClasses;

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

    public List<CourseSettings> getLectures() {
        return lectures;
    }

    public void setLectures(List<CourseSettings> lectures) {
        this.lectures = lectures;
    }

    public int getAuditoryGroupsPerLectureGroup() {
        return auditoryGroupsPerLectureGroup;
    }

    public void setAuditoryGroupsPerLectureGroup(int auditoryGroupsPerLectureGroup) {
        this.auditoryGroupsPerLectureGroup = auditoryGroupsPerLectureGroup;
    }

    public int getLaboratoryGroupsPerAuditoryGroup() {
        return laboratoryGroupsPerAuditoryGroup;
    }

    public void setLaboratoryGroupsPerAuditoryGroup(int laboratoryGroupsPerAuditoryGroup) {
        this.laboratoryGroupsPerAuditoryGroup = laboratoryGroupsPerAuditoryGroup;
    }

    public List<CourseSettings> getAuditoryClasses() {
        return auditoryClasses;
    }

    public void setAuditoryClasses(List<CourseSettings> auditoryClasses) {
        this.auditoryClasses = auditoryClasses;
    }

    public List<CourseSettings> getLaboratoryClasses() {
        return laboratoryClasses;
    }

    public void setLaboratoryClasses(List<CourseSettings> laboratoryClasses) {
        this.laboratoryClasses = laboratoryClasses;
    }
}
