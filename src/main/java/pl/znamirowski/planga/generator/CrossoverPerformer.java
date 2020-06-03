package pl.znamirowski.planga.generator;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class CrossoverPerformer {
    private static final double CROSSOVER_PROBABILITY = 0.75;
    private final AppSettings settings;
    private final Random random;

    public CrossoverPerformer(AppSettings settings) {
        this.settings = settings;
        this.random = new Random();
    }

    public List<Genotype> performCrossover(List<Pair<Genotype, Genotype>> pairs) {
        List<Genotype> newPopulation = new ArrayList<>();
        for (Pair<Genotype, Genotype> pair : pairs) {
            if (random.nextDouble() < CROSSOVER_PROBABILITY) {
                List<Genotype> children = crossoverRegular(pair.getLeft(), pair.getRight());
                newPopulation.addAll(children);
            }
        }
        return newPopulation;
    }

    private List<Genotype> crossoverRegular(Genotype parent1, Genotype parent2) {
        Genotype child1 = generateChildUsingRegularCrossover(parent1, parent2);
        Genotype child2 = generateChildUsingRegularCrossover(parent2, parent1);
        return List.of(child1, child2);
    }

    private Genotype generateChildUsingRegularCrossover(Genotype parent1, Genotype parent2) {
        Genotype child = new Genotype(settings);
        List<LessonTuple> sortedLessons = settings.getLessonTuples().values().stream()
                .sorted(comparingInt(lesson -> -lesson.getTimeUnits()))
                .collect(toList());
        for (LessonTuple lesson : sortedLessons) {
            int indexOfLesson = getIndexOfLesson(lesson.getId(), random.nextBoolean() ? parent1 : parent2);
            child.writeLessonAt(indexOfLesson, lesson);
        }
        return child;
    }

    private int getIndexOfLesson(int lessonNumber, Genotype genotype) {
        int[] genotypeArray = genotype.getGenotype();
        for (int i = 0; i < genotypeArray.length; i++) {
            if (genotypeArray[i] == lessonNumber) {
                return i;
            }
        }
        throw new RuntimeException("Could not find lesson with number " + lessonNumber);
    }
}
