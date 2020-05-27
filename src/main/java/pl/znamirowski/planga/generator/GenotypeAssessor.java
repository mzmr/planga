package pl.znamirowski.planga.generator;

import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
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
    private final int bestTimeWindowToStart;

    public GenotypeAssessor(AppSettings appSettings) {
        this.appSettings = appSettings;
        this.bestTimeWindowToStart = (int) MINUTES.between(LocalTime.parse("08:00"), appSettings.getStartHour())
                / appSettings.getMinutesPerTimeWindow();
    }

    public List<Pair<Genotype, Double>> assessPopulation(List<Genotype> population) {
        return population.stream()
                .map(genotype -> Pair.of(genotype, assessGenotype(genotype)))
                .sorted(comparingDouble(Pair::getRight))
                .collect(toList());
    }

    private double assessGenotype(Genotype genotype) {
        double assessment = 0;
        assessment += GROUP_BREAKS_WEIGHT * calculatePenaltyForGroupBreaks(genotype);
        assessment += GROUP_STARTING_HOUR_WEIGHT * calculatePenaltyForGroupStartingHours(genotype);
        return assessment;
    }

    private double calculatePenaltyForGroupStartingHours(Genotype genotype) {
        int totalPenaltyValue = 0;
        Map<Integer, Boolean> groupIdToDidThisGroupAppearThisDay = appSettings.getGroups().stream().collect(toMap(Group::getId, g -> false));

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            groupIdToDidThisGroupAppearThisDay.replaceAll((key, value) -> false);

            for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
                for (int roomNumber = 0; roomNumber < appSettings.getNumberOfRooms(); roomNumber++) {
                    int lessonIndex = genotype.getLessonIndexAt(dayNumber, timeWindowInDay, roomNumber);
                    if (lessonIndex != -1) {
                        int groupId = appSettings.getLessonTuples().get(lessonIndex).getGroupId();
                        if (!groupIdToDidThisGroupAppearThisDay.get(groupId)) {
                            groupIdToDidThisGroupAppearThisDay.put(groupId, true);
                            totalPenaltyValue += Math.abs(timeWindowInDay - bestTimeWindowToStart);
                        }
                    }
                }
            }
        }

        return totalPenaltyValue;
    }

    private double calculatePenaltyForGroupBreaks(Genotype genotype) {
        int totalPenaltyValue = 0;

        Map<Integer, Boolean> groupIdToDidThisGroupAppearThisDay = appSettings.getGroups().stream().collect(toMap(Group::getId, g -> false));
        Map<Integer, Integer> groupIdToNumberOfBreakWindows = appSettings.getGroups().stream().collect(toMap(Group::getId, g -> 0));
        Map<Integer, Integer> groupIdToNumberOfBreaksAtCurrentWindow = appSettings.getGroups().stream().collect(toMap(Group::getId, g -> 0));

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            groupIdToDidThisGroupAppearThisDay.replaceAll((key, value) -> false);

            for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
                groupIdToNumberOfBreaksAtCurrentWindow.replaceAll((key, value) -> 0);

                for (int roomNumber = 0; roomNumber < appSettings.getNumberOfRooms(); roomNumber++) {
                    int lessonIndex = genotype.getLessonIndexAt(dayNumber, timeWindowInDay, roomNumber);
                    if (lessonIndex == -1) {
                        incrementAll(groupIdToNumberOfBreaksAtCurrentWindow);
                    } else {
                        int groupId = appSettings.getLessonTuples().get(lessonIndex).getGroupId();
                        incrementAll(groupIdToNumberOfBreaksAtCurrentWindow);
                        addValueToEntry(groupIdToNumberOfBreaksAtCurrentWindow, groupId, -1);
                        groupIdToDidThisGroupAppearThisDay.replace(groupId, true);

                        int breakWindows = groupIdToNumberOfBreakWindows.get(groupId);
                        if (breakWindows > 0) {
                            totalPenaltyValue += getPenaltyForBreak(breakWindows);
                        }
                        groupIdToNumberOfBreakWindows.replace(groupId, 0);
                    }
                }

                for (Integer groupId : groupIdToDidThisGroupAppearThisDay.keySet()) {
                    if (groupIdToDidThisGroupAppearThisDay.get(groupId) &&
                            groupIdToNumberOfBreaksAtCurrentWindow.get(groupId) == appSettings.getNumberOfRooms()) {
                        addValueToEntry(groupIdToNumberOfBreakWindows, groupId, 1);
                    }
                }
            }
        }
        return totalPenaltyValue;
    }

    private void addValueToEntry(Map<Integer, Integer> map, int key, int valueToAdd) {
        map.replace(key, map.get(key) + valueToAdd);
    }

    private void incrementAll(Map<Integer, Integer> map) {
        map.replaceAll((key, value) -> value + 1);
    }

    private double getPenaltyForBreak(int numberOfBreakWindows) {
        if (numberOfBreakWindows == 0) {
            return 1;
        }
        if (numberOfBreakWindows == 1) {
            return 0;
        }
        int breakLengthInMinutes = appSettings.getMinutesPerTimeWindow() * numberOfBreakWindows;
        if (breakLengthInMinutes <= 150) {
            return breakLengthInMinutes / 15.0;
        }
        return 12 - (breakLengthInMinutes / 75.0);
    }
}
