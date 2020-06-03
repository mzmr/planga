package pl.znamirowski.planga.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static pl.znamirowski.planga.generator.GroupType.AUDITORY;
import static pl.znamirowski.planga.generator.GroupType.LECTURE;

public class Genotype {
    private final AppSettings appSettings;
    private final Random random;
    private final int[] genotype;

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
        List<LessonTuple> lessonsSortedByDuration = appSettings.getLessonTuples().values().stream()
                .sorted(comparingInt(lesson -> -lesson.getTimeUnits()))
                .collect(toList());
        for (LessonTuple lesson : lessonsSortedByDuration) {
            List<Integer> emptyPositions = findValidPositionsForLesson(lesson);
            int emptyPosition = emptyPositions.get(random.nextInt(emptyPositions.size()));
            Arrays.fill(genotype, emptyPosition, emptyPosition + lesson.getTimeUnits(), lesson.getId());
        }
    }

    public int getLessonIdAt(int dayNumber, int timeWindowInDayNumber, int roomNumber) {
        return genotype[createIndex(dayNumber, timeWindowInDayNumber, roomNumber)];
    }

    public void writeLessonAt(int dayNumber, int roomNumber, int timeWindowNumber, LessonTuple lesson) {
        int timeWindowToWriteLessonAt = findTimeWindowToWriteLessonAt(lesson, timeWindowNumber);
        List<Integer> validPositionsForLesson = findValidPositionsForLesson(lesson);

        if (tryToWriteLessonExactlyAt(dayNumber, roomNumber, timeWindowToWriteLessonAt, lesson, validPositionsForLesson)) {
            return;
        }
        if (tryToWriteLessonAtDayAndTime(dayNumber, roomNumber, timeWindowToWriteLessonAt, lesson, validPositionsForLesson)) {
            return;
        }
        if (tryToWriteLessonAtDayAndRoom(dayNumber, roomNumber, timeWindowToWriteLessonAt, lesson, validPositionsForLesson)) {
            return;
        }
        if (tryToWriteLessonAtDay(dayNumber, roomNumber, timeWindowToWriteLessonAt, lesson, validPositionsForLesson)) {
            return;
        }
        if (tryToWriteLessonAtTimeAndRoom(dayNumber, roomNumber, timeWindowToWriteLessonAt, lesson, validPositionsForLesson)) {
            return;
        }
        if (tryToWriteLessonAtTime(timeWindowToWriteLessonAt, lesson, validPositionsForLesson)) {
            return;
        }
        if (tryToWriteLessonInRoom(roomNumber, timeWindowToWriteLessonAt, lesson, validPositionsForLesson)) {
            return;
        }
        if (tryToWriteLessonRandomly(lesson, validPositionsForLesson)) {
            return;
        }
        throw new RuntimeException("Unable to create correct genotype");
    }

    public void writeLessonAt(int index, LessonTuple lesson) {
        int windowsLeft = index;
        int dayNumber = windowsLeft / (appSettings.getTimeWindowsPerDay() * appSettings.getNumberOfRooms());
        windowsLeft -= dayNumber * appSettings.getTimeWindowsPerDay() * appSettings.getNumberOfRooms();
        int roomNumber = windowsLeft / appSettings.getTimeWindowsPerDay();
        windowsLeft -= roomNumber * appSettings.getTimeWindowsPerDay();
        int timeWindowNumber = windowsLeft;
        writeLessonAt(dayNumber, roomNumber, timeWindowNumber, lesson);
    }

    public List<Integer> findValidPositionsForLesson(LessonTuple newLesson) {
        List<Integer> emptyPositions = new ArrayList<>();
        int numberOfRooms = appSettings.getNumberOfRooms();

        for (int dayNumber = 0; dayNumber < appSettings.getDaysPerWeek(); dayNumber++) {
            int[] numberOfEmptyInEachRoom = new int[numberOfRooms];

            for (int numberOfTimeWindow = 0; numberOfTimeWindow < appSettings.getTimeWindowsPerDay(); numberOfTimeWindow++) {
                List<Integer> temporaryEmptyPositions = new ArrayList<>(numberOfRooms);
                boolean hasAnyRoomLessonWhichIsBlockingNewLesson = false;

                for (int roomNumber = 0; roomNumber < numberOfRooms; roomNumber++) {
                    int index = createIndex(dayNumber, numberOfTimeWindow, roomNumber);
                    int existingLessonId = genotype[index];

                    if (existingLessonId == -1) {
                        numberOfEmptyInEachRoom[roomNumber]++;
                        if (numberOfEmptyInEachRoom[roomNumber] >= newLesson.getTimeUnits()) {
                            temporaryEmptyPositions.add(index - newLesson.getTimeUnits() + 1);
                        }
                    } else {
                        numberOfEmptyInEachRoom[roomNumber] = 0;
                        if (doesExistingLessonBlockNewLesson(existingLessonId, newLesson)) {
                            hasAnyRoomLessonWhichIsBlockingNewLesson = true;
                            break;
                        }
                    }
                }

                if (hasAnyRoomLessonWhichIsBlockingNewLesson) {
                    Arrays.fill(numberOfEmptyInEachRoom, 0);
                } else {
                    emptyPositions.addAll(temporaryEmptyPositions);
                }
            }
        }

        if (emptyPositions.size() == 0) {
            System.out.println("WARNING: Generating timetable can't be finished because of unfortunate lesson positions");
            throw new RuntimeException("WARNING: Generating timetable can't be finished because of unfortunate lesson positions");
        }
        return emptyPositions;
    }

    public int deleteLesson(int lessonIndex) {
        int startIndex = -1;
        for (int i = 0; i < genotype.length; i++) {
            if (genotype[i] == lessonIndex) {
                genotype[i] = -1;
                if (startIndex == -1) {
                    startIndex = i;
                }
            }
        }
        return startIndex;
    }

    private int createIndex(int dayNumber, int numberOfTimeWindow, int roomNumber) {
        return (dayNumber * appSettings.getNumberOfRooms() + roomNumber) * appSettings.getTimeWindowsPerDay()
                + numberOfTimeWindow;
    }

    private boolean tryToWriteLessonRandomly(LessonTuple lesson, List<Integer> validPositions) {
        int validPositionForLesson = validPositions.get(random.nextInt(validPositions.size()));
        writeLessonAndOverride(validPositionForLesson, lesson.getId(), lesson.getTimeUnits());
        return true;
    }

    private boolean tryToWriteLessonInRoom(int roomNumber, int timeWindowNumber, LessonTuple lesson, List<Integer> validPositions) {
        List<Integer> daysToCheck = IntStream.range(0, appSettings.getDaysPerWeek())
                .boxed()
                .collect(toList());
        shuffle(daysToCheck);
        for (int day : daysToCheck) {
            if (tryToWriteLessonAtDayAndRoom(day, roomNumber, timeWindowNumber, lesson, validPositions)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryToWriteLessonAtTime(int timeWindowNumber, LessonTuple lesson, List<Integer> validPositions) {
        List<Integer> daysToCheck = IntStream.range(0, appSettings.getDaysPerWeek())
                .boxed()
                .collect(toList());
        List<Integer> roomsToCheck = IntStream.range(0, appSettings.getNumberOfRooms())
                .boxed()
                .collect(toList());
        shuffle(daysToCheck);
        shuffle(roomsToCheck);
        for (int room : roomsToCheck) {
            for (int day : daysToCheck) {
                if (tryToWriteLessonExactlyAt(day, room, timeWindowNumber, lesson, validPositions)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryToWriteLessonAtTimeAndRoom(int dayNumberToAvoid, int roomNumber, int timeWindowNumber,
                                                  LessonTuple lesson, List<Integer> validPositions) {
        List<Integer> daysToCheck = IntStream.range(0, appSettings.getDaysPerWeek())
                .filter(dayNumber -> dayNumber != dayNumberToAvoid)
                .boxed()
                .collect(toList());
        shuffle(daysToCheck);
        for (int dayNumber : daysToCheck) {
            if (tryToWriteLessonAtDayAndRoom(dayNumber, roomNumber, timeWindowNumber, lesson, validPositions)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryToWriteLessonAtDay(int dayNumber, int roomNumberToAvoid, int timeWindowNumber,
                                          LessonTuple lesson, List<Integer> validPositions) {
        List<Integer> roomsToCheck = IntStream.range(0, appSettings.getNumberOfRooms())
                .filter(roomNumber -> roomNumber != roomNumberToAvoid)
                .boxed()
                .collect(toList());
        shuffle(roomsToCheck);
        for (int roomNumber : roomsToCheck) {
            if (tryToWriteLessonAtDayAndRoom(dayNumber, roomNumber, timeWindowNumber, lesson, validPositions)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryToWriteLessonAtDayAndRoom(int dayNumber, int roomNumber, int timeWindowToStickTo,
                                                 LessonTuple lesson, List<Integer> validPositions) {
        int startIndex = createIndex(dayNumber, 0, roomNumber);
        List<Integer> availablePositions = new ArrayList<>();
        int currentFreeSpaceStart = -1;

        for (int timeWindowInDay = 0; timeWindowInDay < appSettings.getTimeWindowsPerDay(); timeWindowInDay++) {
            if (genotype[startIndex + timeWindowInDay] != -1) {
                currentFreeSpaceStart = timeWindowInDay;
            } else if (timeWindowInDay - currentFreeSpaceStart >= lesson.getTimeUnits()) {
                availablePositions.add(timeWindowInDay - lesson.getTimeUnits() + 1);
            }
        }
        availablePositions = availablePositions.stream()
                .filter(pos -> validPositions.contains(startIndex + pos))
                .collect(toList());
        if (availablePositions.size() > 0) {
            availablePositions.sort(comparingInt(o -> abs(o - timeWindowToStickTo)));
            int timeWindow = availablePositions.get(0);
            writeLessonAndOverride(startIndex + timeWindow, lesson.getId(), lesson.getTimeUnits());
            return true;
        }
        return false;
    }

    private boolean tryToWriteLessonAtDayAndTime(int dayNumber, int roomNumberToAvoid, int timeWindowNumber,
                                                 LessonTuple lesson, List<Integer> validPositions) {
        List<Integer> roomsToCheck = IntStream.range(0, appSettings.getNumberOfRooms())
                .filter(roomNumber -> roomNumber != roomNumberToAvoid)
                .boxed()
                .collect(toList());
        shuffle(roomsToCheck);
        for (int roomNumber : roomsToCheck) {
            if (tryToWriteLessonExactlyAt(dayNumber, roomNumber, timeWindowNumber, lesson, validPositions)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryToWriteLessonExactlyAt(int dayNumber, int roomNumber, int timeWindowNumber, LessonTuple lesson,
                                              List<Integer> validPositions) {
        int numberOfAvailableCells = 0;
        int startingIndex = createIndex(dayNumber, timeWindowNumber, roomNumber);
        if (!validPositions.contains(startingIndex)) {
            return false;
        }

        int numberOfStepsToTheEndOfDay = appSettings.getTimeWindowsPerDay() - timeWindowNumber;
        for (int timeWindowOffset = 0; timeWindowOffset < numberOfStepsToTheEndOfDay; timeWindowOffset++) {
            int lessonIndex = genotype[startingIndex + timeWindowOffset];
            if (lessonIndex != -1) {
                break;
            }
            numberOfAvailableCells++;
            if (numberOfAvailableCells >= lesson.getTimeUnits()) {
                break;
            }
        }
        if (numberOfAvailableCells >= lesson.getTimeUnits()) {
            writeLessonAndOverride(startingIndex, lesson.getId(), lesson.getTimeUnits());
            return true;
        }
        return false;
    }

    private void writeLessonAndOverride(int startIndex, int lessonIdx, int numberOfTimeWindows) {
        for (int timeWindowOffset = 0; timeWindowOffset < numberOfTimeWindows; timeWindowOffset++) {
            genotype[startIndex + timeWindowOffset] = lessonIdx;
        }
    }

    private int findTimeWindowToWriteLessonAt(LessonTuple lesson, int timeWindowNumber) {
        if (lesson.getTimeUnits() > appSettings.getTimeWindowsPerDay() - timeWindowNumber) {
            return Math.max(appSettings.getTimeWindowsPerDay() - lesson.getTimeUnits(), 0);
        }
        return timeWindowNumber;
    }

    private int[] initializeGenotypeArray() {
        int[] genotype = new int[appSettings.getNumberOfRooms() * appSettings.getTimeWindowsPerWeek()];
        Arrays.fill(genotype, -1);
        return genotype;
    }

    private boolean doesExistingLessonBlockNewLesson(int existingLessonId, LessonTuple newLesson) {
        LessonTuple existingLesson = appSettings.getLessonTuples().get(existingLessonId);
        if (existingLesson.getTeacherId() == newLesson.getTeacherId()) {
            return true;
        }
        if (existingLesson.getLectureGroupId() != newLesson.getLectureGroupId()) {
            return false;
        }
        if (existingLesson.getGroupType() == LECTURE || newLesson.getGroupType() == LECTURE) {
            return true;
        }
        if (existingLesson.getAuditoryGroupId() != newLesson.getAuditoryGroupId()) {
            return false;
        }
        if (existingLesson.getGroupType() == AUDITORY || newLesson.getGroupType() == AUDITORY) {
            return true;
        }
        if (existingLesson.getLaboratoryGroupId() != newLesson.getLaboratoryGroupId()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Genotype{" +
                Arrays.toString(genotype).replace("-1", "-") +
                '}';
    }
}
