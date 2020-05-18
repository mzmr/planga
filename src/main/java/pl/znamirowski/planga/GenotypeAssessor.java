package pl.znamirowski.planga;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class GenotypeAssessor {
    /*
//    Teacher conflict - teachers can't take more than one lesson at the same time
//    Group conflict - groups can't have more than one lesson at the same time

    Group breaks - groups should have 15 minutes (or 0 minutes) break between lessons
    Group starting hour - groups should start lessons at 8am
    Group regular lessons - groups should have lessons placed regularly in all days in a week

    Teacher breaks - teachers should have 15 minutes (or 0 minutes) break between lessons
    Teacher lessons a day - teachers should not exceed the maximum number of lessons allowed per day


    Break (?) - penalty for break >15 minutes is less if a break is very big (the worst is 2h)
    */

    private static final double GROUP_BREAKS_WEIGHT = 1;
    private static final double GROUP_STARTING_HOUR_WEIGHT = 1;
    private static final double GROUP_REGULAR_LESSONS_WEIGHT = 1;
    private static final double TEACHER_BREAKS_WEIGHT = 1;
    private static final double TEACHER_LESSONS_A_DAY_WEIGHT = 1;
    private final AppSettings appSettings;

    public GenotypeAssessor(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public double assessGenotype(Genotype genotype) {
        int[] genotypeArray = genotype.getGenotype();
        double assessment = 0;
        assessment += GROUP_BREAKS_WEIGHT * calculatePenaltyForGroupBreaks(genotypeArray);
        return assessment;
    }

    private double calculatePenaltyForGroupBreaks(int[] genotype) {
        int totalPenaltyValue = 0;
        int numberOfRooms = appSettings.getNumberOfRooms();

        Map<Integer, Boolean> wasSomeLessonBeforeThisDayForGroups = appSettings.getGroups().stream().collect(toMap(Group::getId, g -> false));
        Map<Integer, Integer> numberOfBreakWindowsForGroups = appSettings.getGroups().stream().collect(toMap(Group::getId, g -> 0));
        Map<Integer, Integer> numberOfBreaksAtCurrentWindowForGroups = appSettings.getGroups().stream().collect(toMap(Group::getId, g -> 0));

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            wasSomeLessonBeforeThisDayForGroups.replaceAll((key, value) -> false);

            for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
                numberOfBreaksAtCurrentWindowForGroups.replaceAll((key, value) -> 0);

                for (int roomNumber = 0; roomNumber < numberOfRooms; roomNumber++) {
                    int genotypeIndex = dayNumber * numberOfRooms * appSettings.getTimeWindowsPerDay()
                            + roomNumber * appSettings.getTimeWindowsPerDay()
                            + timeWindowInDay;
                    int lessonTupleIndex = genotype[genotypeIndex];
                    if (lessonTupleIndex == -1) {
                        incrementAll(numberOfBreaksAtCurrentWindowForGroups);
                    } else {
                        int groupId = appSettings.getLessonTuples().get(lessonTupleIndex).getGroupId();
                        incrementAll(numberOfBreaksAtCurrentWindowForGroups);
                        numberOfBreaksAtCurrentWindowForGroups.replace(groupId, numberOfBreaksAtCurrentWindowForGroups.get(groupId) - 1);
                        wasSomeLessonBeforeThisDayForGroups.replace(groupId, true);

                        Integer breakWindows = numberOfBreakWindowsForGroups.get(groupId);
                        if (breakWindows > 0) {
                            totalPenaltyValue += getPenaltyForBreak(appSettings.getMinutesPerTimeWindow() * breakWindows);
                        }
                    }
                }

                for (Integer groupId : wasSomeLessonBeforeThisDayForGroups.keySet()) {
                    if (wasSomeLessonBeforeThisDayForGroups.get(groupId) &&
                            numberOfBreaksAtCurrentWindowForGroups.get(groupId) == numberOfRooms) {
                        numberOfBreakWindowsForGroups.replace(groupId, numberOfBreakWindowsForGroups.get(groupId) + 1);
                    }
                }
            }
        }
        return totalPenaltyValue;
    }

    private void incrementAll(Map<Integer, Integer> map) {
        map.replaceAll((key, value) -> value + 1);
    }

    private double getPenaltyForBreak(int breakLengthInMinutes) {
        if (breakLengthInMinutes == 0) {
            return 1;
        }
        if (breakLengthInMinutes == appSettings.getMinutesPerTimeWindow()) {
            return 0;
        }
        if (breakLengthInMinutes <= 150) {
            return breakLengthInMinutes / 15.0;
        }
        return 12 - (breakLengthInMinutes / 75.0);
    }
}
