package pl.znamirowski.planga;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

    private final Settings settings;

    public TimetableGenerator(Settings settings) {
        this.settings = settings;
    }

    public void generateTimetable() {
        TimeSettings timeSettings = new TimeSettings(settings);
        List<LessonTuple> lessonTuples = createLessonTuples();
        List<Genotype> population = initializePopulation(lessonTuples, timeSettings);
        List<Pair<Genotype, Double>> assessedPopulation = assessPopulation(population, lessonTuples, timeSettings);
    }

    private List<LessonTuple> createLessonTuples() {
        List<LessonTuple> lessonTuples = new ArrayList<>();
//        int currentId = 0;
        for (Group group : settings.getGroups()) {
            for (CourseSettings course : group.getCourses()) {
                LessonTuple lessonTuple = new LessonTuple();
//                lessonTuple.setId(currentId++);
                lessonTuple.setGroupId(group.getId());
                lessonTuple.setCourseId(course.getCourseId());
                lessonTuple.setTeacherId(course.getTeacherId());
                lessonTuple.setTimeUnits(course.getTimeUnits());
                lessonTuples.add(lessonTuple);
            }
        }
        return lessonTuples;
    }

    private List<Genotype> initializePopulation(List<LessonTuple> lessonTuples, TimeSettings timeSettings) {
        List<Genotype> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Genotype(settings.getNumberOfRooms(), timeSettings, lessonTuples));
        }
        return population;
    }

    private List<Pair<Genotype, Double>> assessPopulation(List<Genotype> population, List<LessonTuple> lessonTuples,
                                                          TimeSettings timeSettings) {
        GenotypeAssessor assessor = new GenotypeAssessor(lessonTuples, settings, timeSettings);
        return population.stream()
                .map(genotype -> Pair.of(genotype, assessor.assessGenotype(genotype)))
                .collect(toList());
    }
}
