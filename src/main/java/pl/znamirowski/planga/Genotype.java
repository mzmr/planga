package pl.znamirowski.planga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Genotype {
    private final TimeSettings timeSettings;
    private final Random random;
    private int[] genotype;

    public Genotype(int numberOfRooms, TimeSettings timeSettings, List<LessonTuple> lessonTuples) {
        this.random = new Random();
        this.timeSettings = timeSettings;
        int[] emptyGenotype = initializeGenotypeArray(numberOfRooms);
        genotype = fillRandomlyWithLessons(emptyGenotype, lessonTuples, numberOfRooms);
    }

    public int[] getGenotype() {
        return genotype;
    }

    private int[] initializeGenotypeArray(int numberOfRooms) {
        int[] genotype = new int[numberOfRooms * timeSettings.getTimeWindowsPerWeek()];
        Arrays.fill(genotype, -1);
        return genotype;
    }

    private int[] fillRandomlyWithLessons(int[] genotype, List<LessonTuple> lessonTuples, int numberOfRooms) {
        for (int i = 0; i < lessonTuples.size(); i++) {
            LessonTuple lessonTuple = lessonTuples.get(i);
            int emptyPosition = findRandomEmptyValidPosition(genotype, lessonTuple, lessonTuples, numberOfRooms);
            Arrays.fill(genotype, emptyPosition, emptyPosition + lessonTuple.getTimeUnits(), i);
        }
        return genotype;
    }

    private int findRandomEmptyValidPosition(int[] genotype, LessonTuple lessonTuple, List<LessonTuple> lessonTuples,
                                             int numberOfRooms) {
        List<Integer> emptyPositions = new ArrayList<>();
        int timeWindowsPerDay = timeSettings.getTimeWindowsPerDay();
        int numberOfDays = genotype.length / timeWindowsPerDay / numberOfRooms;

        for (int dayNumber = 0; dayNumber < numberOfDays; dayNumber++) {
            int[] numberOfEmptyInEachRoom = new int[numberOfRooms];

            for (int numberOfTimeWindow = 0; numberOfTimeWindow < timeWindowsPerDay; numberOfTimeWindow++) {
                List<Integer> temporaryEmptyPositions = new ArrayList<>(numberOfRooms);
                boolean hasAnyRoomLessonWithTeacherOrGroupAsNewLesson = false;

                for (int roomNumber = 0; roomNumber < numberOfRooms; roomNumber++) {
                    int index = createIndex(dayNumber, numberOfTimeWindow, roomNumber, numberOfRooms);
                    int lessonTupleIndex = genotype[index];

                    if (lessonTupleIndex == -1) {
                        if (++numberOfEmptyInEachRoom[roomNumber] >= lessonTuple.getTimeUnits()) {
                            temporaryEmptyPositions.add(index - lessonTuple.getTimeUnits() + 1);
                        }
                    } else if (hasLessonSameTeacherOrGroupAsNewLesson(lessonTuples.get(lessonTupleIndex), lessonTuple)) {
                        hasAnyRoomLessonWithTeacherOrGroupAsNewLesson = true;
                        break;
                    }
                }

                if (hasAnyRoomLessonWithTeacherOrGroupAsNewLesson) {
                    Arrays.fill(numberOfEmptyInEachRoom, 0);
                } else {
                    emptyPositions.addAll(temporaryEmptyPositions);
                }
            }
        }

        if (emptyPositions.size() == 0) {
            System.out.println("WARNING: Generating timetable can't be finished because of unfortunate lesson positions");
            return findRandomEmptyValidPosition(genotype, lessonTuple, lessonTuples, numberOfRooms);
        }
        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }

    private boolean hasLessonSameTeacherOrGroupAsNewLesson(LessonTuple existingLesson, LessonTuple newLesson) {
        if (existingLesson.getTeacherId() == newLesson.getTeacherId()) {
            return true;
        }
        if (existingLesson.getGroupId() == newLesson.getGroupId()) {
            return true;
        }
        return false;
    }

    private int createIndex(int dayNumber, int numberOfTimeWindow, int roomNumber, int numberOfRooms) {
        return (dayNumber * numberOfRooms + roomNumber) * timeSettings.getTimeWindowsPerDay() + numberOfTimeWindow;
    }
}
