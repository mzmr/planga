package pl.znamirowski.planga.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Genotype {
    private final AppSettings appSettings;
    private final Random random;
    private int[] genotype;

    public Genotype(AppSettings appSettings) {
        this.random = new Random();
        this.appSettings = appSettings;
        genotype = initializeGenotypeArray();
    }

    public int[] getGenotype() {
        return genotype;
    }

    public AppSettings getSettings() {
        return appSettings;
    }

    public void fillRandomlyWithLessons() {
        List<LessonTuple> lessons = appSettings.getLessonTuples();
        for (int i = 0; i < lessons.size(); i++) {
            LessonTuple lesson = lessons.get(i);
            int emptyPosition = findValidPositionForLesson(lesson);
            Arrays.fill(genotype, emptyPosition, emptyPosition + lesson.getTimeUnits(), i);
        }
    }

    public int getLessonIndexAt(int dayNumber, int timeWindowInDayNumber, int roomNumber) {
        return genotype[createIndex(dayNumber, timeWindowInDayNumber, roomNumber)];
    }

    private int createIndex(int dayNumber, int numberOfTimeWindow, int roomNumber) {
        return (dayNumber * appSettings.getNumberOfRooms() + roomNumber) * appSettings.getTimeWindowsPerDay() + numberOfTimeWindow;
    }

    private int[] initializeGenotypeArray() {
        int[] genotype = new int[appSettings.getNumberOfRooms() * appSettings.getTimeWindowsPerWeek()];
        Arrays.fill(genotype, -1);
        return genotype;
    }

    private int findValidPositionForLesson(LessonTuple lesson) {
        List<Integer> emptyPositions = new ArrayList<>();
        int numberOfRooms = appSettings.getNumberOfRooms();

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            int[] numberOfEmptyInEachRoom = new int[numberOfRooms];

            for (int numberOfTimeWindow = 0; numberOfTimeWindow < appSettings.getTimeWindowsPerDay(); numberOfTimeWindow++) {
                List<Integer> temporaryEmptyPositions = new ArrayList<>(numberOfRooms);
                boolean hasAnyRoomLessonWithTeacherOrGroupAsNewLesson = false;

                for (int roomNumber = 0; roomNumber < numberOfRooms; roomNumber++) {
                    int index = createIndex(dayNumber, numberOfTimeWindow, roomNumber);
                    int lessonIndex = genotype[index];

                    if (lessonIndex == -1) {
                        if (++numberOfEmptyInEachRoom[roomNumber] >= lesson.getTimeUnits()) {
                            temporaryEmptyPositions.add(index - lesson.getTimeUnits() + 1);
                        }
                    } else if (hasLessonWithIndexSameTeacherOrGroupAsNewLesson(lessonIndex, lesson)) {
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
            return findValidPositionForLesson(lesson);
        }
        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }

    private boolean hasLessonWithIndexSameTeacherOrGroupAsNewLesson(int existingLessonIndex, LessonTuple newLesson) {
        LessonTuple existingLesson = appSettings.getLessonTuples().get(existingLessonIndex);
        if (existingLesson.getTeacherId() == newLesson.getTeacherId()) {
            return true;
        }
        if (existingLesson.getGroupId() == newLesson.getGroupId()) {
            return true;
        }
        return false;
    }
}
