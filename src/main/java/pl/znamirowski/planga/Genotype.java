package pl.znamirowski.planga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Genotype {
    private final int timeWindowsPerDay;
    private final Random random;
    private int[] genotype;

    public Genotype(int numberOfRooms, int timeWindowsPerWeek, int timeWindowsPerDay, List<LessonTuple> lessonTuples) {
        this.random = new Random();
        this.timeWindowsPerDay = timeWindowsPerDay;
        int[] emptyGenotype = initializeGenotypeArray(numberOfRooms, timeWindowsPerWeek);
        genotype = fillRandomlyAllowingInvalidConfig(emptyGenotype, lessonTuples);
    }

    private int[] initializeGenotypeArray(int numberOfRooms, int timeWindowsPerWeek) {
        int[] genotype = new int[numberOfRooms * timeWindowsPerWeek];
        Arrays.fill(genotype, -1);
        return genotype;
    }

    private int[] fillRandomlyAllowingInvalidConfig(int[] genotype, List<LessonTuple> lessonTuples) {
        for (int i = 0; i < lessonTuples.size(); i++) {
            int timeWindows = lessonTuples.get(i).getTimeUnits();
            int emptyPosition = findRandomEmptyPosition(genotype, timeWindows);
            Arrays.fill(genotype, emptyPosition, emptyPosition + timeWindows, i);
        }
        return genotype;
    }

    private int findRandomEmptyPosition(int[] genotype, int timeWindowsNeeded) {
        List<Integer> emptyPositions = new ArrayList<>();
        int numberOfDays = genotype.length / timeWindowsPerDay;
        for (int dayNumber = 0; dayNumber < numberOfDays; dayNumber++) {
            int numberOfEmpty = 0;
            for (int numberOfTimeWindow = 0; numberOfTimeWindow < timeWindowsPerDay; numberOfTimeWindow++) {
                int index = dayNumber * timeWindowsPerDay + numberOfTimeWindow;
                if (genotype[index] == -1) {
                    numberOfEmpty++;
                    if (numberOfEmpty >= timeWindowsNeeded) {
                        emptyPositions.add(index - timeWindowsNeeded + 1);
                    }
                } else {
                    numberOfEmpty = 0;
                }
            }
        }
        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }
}
