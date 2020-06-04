package pl.znamirowski.planga.generator.settings;

public class LessonTuple {
    private final int id;
    private final GroupTuple groupTuple;
    private final int courseId;
    private final int teacherId;
    private final int timeUnits;

    public LessonTuple(int id, GroupTuple groupTuple, int courseId, int teacherId, int timeUnits) {
        this.id = id;
        this.groupTuple = groupTuple;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.timeUnits = timeUnits;
    }

    public int getId() {
        return id;
    }

    public GroupTuple getGroupTuple() {
        return groupTuple;
    }

    public GroupType getGroupType() {
        return groupTuple.getGroupType();
    }

    public int getLectureGroupId() {
        return groupTuple.getLectureGroupId();
    }

    public int getAuditoryGroupId() {
        return groupTuple.getAuditoryGroupId();
    }

    public int getLaboratoryGroupId() {
        return groupTuple.getLaboratoryGroupId();
    }

    public int getCourseId() {
        return courseId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public int getTimeUnits() {
        return timeUnits;
    }
}
