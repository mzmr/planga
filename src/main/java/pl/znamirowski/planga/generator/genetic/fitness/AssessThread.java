package pl.znamirowski.planga.generator.genetic.fitness;

import pl.znamirowski.planga.generator.genetic.Genotype;
import pl.znamirowski.planga.generator.settings.AppSettings;
import pl.znamirowski.planga.generator.settings.GroupTuple;
import pl.znamirowski.planga.generator.settings.LessonTuple;
import pl.znamirowski.planga.generator.settings.Teacher;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static pl.znamirowski.planga.generator.settings.GroupType.AUDITORY;
import static pl.znamirowski.planga.generator.settings.GroupType.LABORATORY;
import static pl.znamirowski.planga.generator.settings.GroupType.LECTURE;

public class AssessThread implements Callable<Double> {
    private static final double GROUP_BREAKS_WEIGHT = 5;
    private static final double GROUP_STARTING_HOUR_WEIGHT = 1;
    private static final double GROUP_REGULAR_LESSONS_WEIGHT = 1;
    private static final double TEACHER_BREAKS_WEIGHT = 3;
    private static final double TEACHER_LESSONS_A_DAY_WEIGHT = 1;
    private final AppSettings appSettings;
    private final Genotype genotype;
    private final int bestTimeWindowToStart;

    public AssessThread(AppSettings settings, Genotype genotype, int bestTimeWindowToStart) {
        this.appSettings = settings;
        this.genotype = genotype;
        this.bestTimeWindowToStart = bestTimeWindowToStart;
    }

    @Override
    public Double call() {
        return assessGenotype();
    }

    private double assessGenotype() {
        List<GroupTuple> laboratoryTuples = appSettings.getGroupTuples().stream()
                .filter(groupTuple -> groupTuple.getGroupType() == LABORATORY)
                .collect(toList());
        double assessment = 0;
        assessment += GROUP_BREAKS_WEIGHT * calculatePenaltyForGroupBreaks(genotype, laboratoryTuples);
        assessment += GROUP_STARTING_HOUR_WEIGHT * calculatePenaltyForGroupStartingHours(genotype, laboratoryTuples);
        assessment += GROUP_REGULAR_LESSONS_WEIGHT * calculatePenaltyForNonRegularLessons(genotype, laboratoryTuples);
        assessment += TEACHER_BREAKS_WEIGHT * calculatePenaltyForTeacherBreaks(genotype);
        return assessment;
    }

    private double calculatePenaltyForTeacherBreaks(Genotype genotype) {
        int totalPenaltyValue = 0;

        Map<Integer, Boolean> teacherIdToDidHeHaveLessonToday = appSettings.getTeachers().stream().collect(toMap(Teacher::getId, g -> false));
        Map<Integer, Integer> teacherIdToNumberOfBreakWindows = appSettings.getTeachers().stream().collect(toMap(Teacher::getId, g -> 0));
        Map<Integer, Integer> teacherIdToNumberOfBreaksAtCurrentWindow = appSettings.getTeachers().stream().collect(toMap(Teacher::getId, g -> 0));

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            teacherIdToDidHeHaveLessonToday.replaceAll((key, value) -> false);

            for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
                teacherIdToNumberOfBreaksAtCurrentWindow.replaceAll((key, value) -> 0);

                for (int roomNumber = 0; roomNumber < appSettings.getNumberOfRooms(); roomNumber++) {
                    int lessonId = genotype.getLessonIdAt(dayNumber, timeWindowInDay, roomNumber);
                    if (lessonId == -1) {
                        incrementAll(teacherIdToNumberOfBreaksAtCurrentWindow);
                    } else {
                        int teacherId = appSettings.getLessonTuples().get(lessonId).getTeacherId();
                        incrementAll(teacherIdToNumberOfBreaksAtCurrentWindow);
                        addValueToEntry(teacherIdToNumberOfBreaksAtCurrentWindow, teacherId, -1);
                        teacherIdToDidHeHaveLessonToday.replace(teacherId, true);

                        int breakWindows = teacherIdToNumberOfBreakWindows.get(teacherId);
                        if (breakWindows > 0) {
                            totalPenaltyValue += calculatePenaltyForBreak(breakWindows);
                        }
                        teacherIdToNumberOfBreakWindows.replace(teacherId, 0);
                    }
                }

                for (Integer teacherId : teacherIdToDidHeHaveLessonToday.keySet()) {
                    if (teacherIdToDidHeHaveLessonToday.get(teacherId) &&
                            teacherIdToNumberOfBreaksAtCurrentWindow.get(teacherId) == appSettings.getNumberOfRooms()) {
                        addValueToEntry(teacherIdToNumberOfBreakWindows, teacherId, 1);
                    }
                }
            }
        }
        return totalPenaltyValue;
    }

    private double calculatePenaltyForNonRegularLessons(Genotype genotype, List<GroupTuple> laboratoryTuples) {
        int totalPenaltyValue = 0;
        Map<GroupTuple, Integer> labTupleToNumberOfLessonWindowsInWeek = laboratoryTuples.stream().collect(toMap(Function.identity(), t -> 0));
        for (LessonTuple lesson : appSettings.getLessonTuples().values()) {
            Set<GroupTuple> labsMatchingLesson = laboratoryTuples.stream()
                    .filter(labGroup -> isLabGroupInGroup(labGroup, lesson.getGroupTuple()))
                    .collect(toSet());
            for (GroupTuple labMatchingLesson : labsMatchingLesson) {
                labTupleToNumberOfLessonWindowsInWeek.replace(labMatchingLesson,
                        labTupleToNumberOfLessonWindowsInWeek.get(labMatchingLesson) + lesson.getTimeUnits());
            }
        }

        Map<GroupTuple, Integer> labTupleToNumberOfLessonsInDay = laboratoryTuples.stream()
                .collect(toMap(Function.identity(), g -> 0));

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            labTupleToNumberOfLessonsInDay.replaceAll((key, value) -> 0);

            for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
                for (int roomNumber = 0; roomNumber < appSettings.getNumberOfRooms(); roomNumber++) {
                    int lessonId = genotype.getLessonIdAt(dayNumber, timeWindowInDay, roomNumber);
                    if (lessonId != -1) {
                        GroupTuple groupTuple = appSettings.getLessonTuples().get(lessonId).getGroupTuple();
                        Set<GroupTuple> labsMatchingLesson = laboratoryTuples.stream()
                                .filter(labGroup -> isLabGroupInGroup(labGroup, groupTuple))
                                .collect(toSet());
                        for (GroupTuple labMatchingLesson : labsMatchingLesson) {
                            labTupleToNumberOfLessonsInDay.replace(labMatchingLesson,
                                    labTupleToNumberOfLessonsInDay.get(labMatchingLesson) + 1);
                        }
                    }
                }
            }

            for (Map.Entry<GroupTuple, Integer> labLessonsInDay : labTupleToNumberOfLessonsInDay.entrySet()) {
                double dailyMean = ((double) labTupleToNumberOfLessonWindowsInWeek.get(labLessonsInDay.getKey()))
                        / appSettings.getDaysPerWeek();
                totalPenaltyValue += Math.abs(labLessonsInDay.getValue() - dailyMean);
            }
        }

        return totalPenaltyValue;
    }

    private double calculatePenaltyForGroupStartingHours(Genotype genotype, List<GroupTuple> laboratoryTuples) {
        int totalPenaltyValue = 0;
        Map<GroupTuple, Boolean> labTupleToDidThisGroupHaveLessonToday = laboratoryTuples.stream()
                .collect(toMap(Function.identity(), g -> false));

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            labTupleToDidThisGroupHaveLessonToday.replaceAll((key, value) -> false);

            for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
                for (int roomNumber = 0; roomNumber < appSettings.getNumberOfRooms(); roomNumber++) {
                    int lessonId = genotype.getLessonIdAt(dayNumber, timeWindowInDay, roomNumber);
                    if (lessonId != -1) {
                        GroupTuple groupTuple = appSettings.getLessonTuples().get(lessonId).getGroupTuple();
                        Set<GroupTuple> labsMatchingGroup = laboratoryTuples.stream()
                                .filter(labGroup -> isLabGroupInGroup(labGroup, groupTuple))
                                .collect(toSet());
                        for (GroupTuple labInGroup : labsMatchingGroup) {
                            if (!labTupleToDidThisGroupHaveLessonToday.get(labInGroup)) {
                                labTupleToDidThisGroupHaveLessonToday.put(labInGroup, true);
                                totalPenaltyValue += Math.abs(timeWindowInDay - bestTimeWindowToStart);
                            }
                        }
                    }
                }
            }
        }

        return totalPenaltyValue;
    }

    private double calculatePenaltyForGroupBreaks(Genotype genotype, List<GroupTuple> laboratoryTuples) {
        int totalPenaltyValue = 0;
        Map<GroupTuple, Boolean> labTupleToDidThisGroupHaveLessonToday = laboratoryTuples.stream()
                .collect(toMap(Function.identity(), g -> false));
        Map<GroupTuple, Integer> labTupleToNumberOfBreakWindows = laboratoryTuples.stream()
                .collect(toMap(Function.identity(), g -> 0));
        Map<GroupTuple, Integer> labTupleToNumberOfBreaksAtCurrentWindow = laboratoryTuples.stream()
                .collect(toMap(Function.identity(), g -> 0));

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            labTupleToDidThisGroupHaveLessonToday.replaceAll((key, value) -> false);

            for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
                labTupleToNumberOfBreaksAtCurrentWindow.replaceAll((key, value) -> 0);

                for (int roomNumber = 0; roomNumber < appSettings.getNumberOfRooms(); roomNumber++) {
                    int lessonId = genotype.getLessonIdAt(dayNumber, timeWindowInDay, roomNumber);
                    if (lessonId == -1) {
                        incrementAll(labTupleToNumberOfBreaksAtCurrentWindow);
                    } else {
                        GroupTuple groupTuple = appSettings.getLessonTuples().get(lessonId).getGroupTuple();
                        Set<GroupTuple> labsMatchingGroup = laboratoryTuples.stream()
                                .filter(labGroup -> isLabGroupInGroup(labGroup, groupTuple))
                                .collect(toSet());
                        incrementAllThatDoNotMatchTheGroup(labTupleToNumberOfBreaksAtCurrentWindow, labsMatchingGroup);
                        replaceAllThatMatchTheGroup(labTupleToDidThisGroupHaveLessonToday, labsMatchingGroup, true);

                        for (GroupTuple labInGroup : labsMatchingGroup) {
                            int breakWindows = labTupleToNumberOfBreakWindows.get(labInGroup);
                            if (breakWindows > 0) {
                                totalPenaltyValue += calculatePenaltyForBreak(breakWindows);
                            }
                            labTupleToNumberOfBreakWindows.replace(labInGroup, 0);
                        }
                    }
                }

                for (GroupTuple groupTuple : labTupleToDidThisGroupHaveLessonToday.keySet()) {
                    if (labTupleToDidThisGroupHaveLessonToday.get(groupTuple) &&
                            labTupleToNumberOfBreaksAtCurrentWindow.get(groupTuple) == appSettings.getNumberOfRooms()) {
                        addValueToEntry(labTupleToNumberOfBreakWindows, groupTuple, 1);
                    }
                }
            }
        }
        return totalPenaltyValue;
    }

    private <T> void addValueToEntry(Map<T, Integer> map, T key, int valueToAdd) {
        map.replace(key, map.get(key) + valueToAdd);
    }

    private void incrementAll(Map<?, Integer> map) {
        map.replaceAll((key, value) -> value + 1);
    }

    private void incrementAllThatDoNotMatchTheGroup(Map<GroupTuple, Integer> groupTuples, Set<GroupTuple> matchingTheGroup) {
        groupTuples.replaceAll((labGroup, value) -> matchingTheGroup.contains(labGroup) ? value : value + 1);
    }

    private void replaceAllThatMatchTheGroup(Map<GroupTuple, Boolean> groupTuples, Set<GroupTuple> matchingTheGroup,
                                             boolean valueToSet) {
        groupTuples.replaceAll((group, value) -> matchingTheGroup.contains(group) ? valueToSet : value);
    }

    private double calculatePenaltyForBreak(int numberOfBreakWindows) {
        if (numberOfBreakWindows == 1) {
            return -1;
        }
        int breakLengthInMinutes = appSettings.getMinutesPerTimeWindow() * numberOfBreakWindows;
        if (breakLengthInMinutes <= 150) {
            return breakLengthInMinutes / 15.0;
        }
        return 12 - (breakLengthInMinutes / 75.0);
    }

    private boolean isLabGroupInGroup(GroupTuple labGroup, GroupTuple superiorGroup) {
        if (labGroup.getLectureGroupId() != superiorGroup.getLectureGroupId()) {
            return false;
        }
        if (superiorGroup.getGroupType() == LECTURE) {
            return true;
        }
        if (labGroup.getAuditoryGroupId() != superiorGroup.getAuditoryGroupId()) {
            return false;
        }
        if (superiorGroup.getGroupType() == AUDITORY) {
            return true;
        }
        if (labGroup.getLaboratoryGroupId() != superiorGroup.getLaboratoryGroupId()) {
            return false;
        }
        return true;
    }
}
