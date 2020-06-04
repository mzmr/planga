package pl.znamirowski.planga.generator;

public class GroupTuple {
    private final GroupType groupType;
    private final int lectureGroupId;
    private final int auditoryGroupId;
    private final int laboratoryGroupId;

    public GroupTuple(GroupType groupType, int lectureGroupId, int auditoryGroupId, int laboratoryGroupId) {
        this.groupType = groupType;
        this.lectureGroupId = lectureGroupId;
        this.auditoryGroupId = auditoryGroupId;
        this.laboratoryGroupId = laboratoryGroupId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupTuple that = (GroupTuple) o;

        if (lectureGroupId != that.lectureGroupId) return false;
        if (auditoryGroupId != that.auditoryGroupId) return false;
        if (laboratoryGroupId != that.laboratoryGroupId) return false;
        return groupType == that.groupType;
    }

    @Override
    public int hashCode() {
        int result = groupType.hashCode();
        result = 31 * result + lectureGroupId;
        result = 31 * result + auditoryGroupId;
        result = 31 * result + laboratoryGroupId;
        return result;
    }
}
