package pl.znamirowski.planga;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

public class TimetableGenerator {
    private static final int POPULATION_SIZE = 100;
    private static final double CROSSOVER_PROBABILITY = 0.75;
    private static final double MUTATION_PROBABILITY = 0.02;

    /*

    1. mamy populację początkową
    2. używamy rankingowej metody selekcji
    3. sortujemy osobniki wg przystosowania i obliczamy każdemu współczynnik selekcji
    3.5. współczynnik selekcji można obliczyć np. funkcją: w = (N - i)^1.5, czyli dla N=100, pierwszy (zerowy) dostanie wartość 1000, a ostatni 1
    4. obliczamy szansę, że dany osobnik zostanie rodzicem, np.: p = w / (N^1.5)
    4.5. dla każdego osobnika losujemy liczbę 0-1. Jeśli jest mniejsza niż p, to zostaje on rodzicem
    5. z grupy rodziców losujemy pary
    6. dla każdej pary losujemy liczbę 0-1 -> jeśli jest mniejsza niż prawdopodobieństwo krzyżowania, to krzyżujemy
    7. dla każdej pary, która będzie krzyżowana, losujemy miejsca krzyżowania (podziału chromosomów)
    8. krzyżujemy pary, otrzymując dla każdej dwóch potomków
    9. dla każdego osobnika losujemy liczbę 0-1 -> jeśli jest mniejsza niż prawdopodobieństwo mutacji, to mutujemy

    10. uwaga: nowe osobniki całkowicie zastępują stare, poza pozostawionym 1% najlepszych
    (zostawienie jednego najlepszego osobnika to strategia elitarna)

     */


    public void generateTimetable(Settings settings) {
        List<LessonTuple> lessonTuples = createLessonTuples(settings);
        initializePopulation(settings, lessonTuples);
    }

    private List<LessonTuple> createLessonTuples(Settings settings) {
        List<LessonTuple> lessonTuples = new ArrayList<>();
        int currentId = 0;
        for (Group group : settings.getGroups()) {
            for (CourseSettings course : group.getCourses()) {
                LessonTuple lessonTuple = new LessonTuple();
                lessonTuple.setId(currentId++);
                lessonTuple.setGroupId(group.getId());
                lessonTuple.setCourseId(course.getCourseId());
                lessonTuple.setTeacherId(course.getTeacherId());
                lessonTuple.setTimeUnits(course.getTimeUnits());
                lessonTuples.add(lessonTuple);
            }
        }
        return lessonTuples;
    }

    private List<Genotype> initializePopulation(Settings settings, List<LessonTuple> lessonTuples) {
        int daysInWeek = 2;
        long minutesInDay = MINUTES.between(settings.getStartHour(), settings.getEndHour());
        long minutesInTimeStep = MINUTES.between(LocalTime.parse("00:00"), settings.getTimeStep());
        int timeWindowsPerDay = (int) (minutesInDay / minutesInTimeStep);
        int timeWindowsPerWeek = timeWindowsPerDay * daysInWeek;

        List<Genotype> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Genotype(settings.getRooms(), timeWindowsPerWeek, timeWindowsPerDay, lessonTuples));
        }
        return population;
    }
}
