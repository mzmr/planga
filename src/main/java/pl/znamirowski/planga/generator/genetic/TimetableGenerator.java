package pl.znamirowski.planga.generator.genetic;

import org.apache.commons.lang3.tuple.Pair;
import pl.znamirowski.planga.generator.genetic.fitness.GenotypeAssessor;
import pl.znamirowski.planga.generator.genetic.operator.CrossoverPerformer;
import pl.znamirowski.planga.generator.genetic.operator.MutationPerformer;
import pl.znamirowski.planga.generator.settings.AppSettings;
import pl.znamirowski.planga.generator.settings.input.InputSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.stream.Collectors.toList;

public class TimetableGenerator {
    private static final int POPULATION_SIZE = 100;
    private static final int NUMBER_OF_GENERATIONS = 100;

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

     */

    private final AppSettings appSettings;
    private final Random random;
    private final GenotypeAssessor genotypeAssessor;
    private final CrossoverPerformer crossoverPerformer;
    private final MutationPerformer mutationPerformer;

    public TimetableGenerator(InputSettings inputSettings) {
        appSettings = new AppSettings(inputSettings);
        random = new Random();
        genotypeAssessor = new GenotypeAssessor(appSettings);
        crossoverPerformer = new CrossoverPerformer(appSettings);
        mutationPerformer = new MutationPerformer(appSettings);
    }

    public Genotype generateTimetable() {
        List<Genotype> population = initializePopulation();
        List<Pair<Genotype, Double>> assessedPopulation = genotypeAssessor.assessPopulation(population);
        System.out.println("population size: " + population.size());
        for (int generationNumber = 0; generationNumber < NUMBER_OF_GENERATIONS; generationNumber++) {
            List<Genotype> parents = chooseParents(assessedPopulation);
            List<Pair<Genotype, Genotype>> pairs = pairParents(parents);
            List<Genotype> newGenotypes = crossoverPerformer.performCrossover(pairs);
            population = createNewGeneration(assessedPopulation, newGenotypes);
            int numberOfMutations = mutationPerformer.performMutation(population);
            assessedPopulation = genotypeAssessor.assessPopulation(population);
            assessedPopulation = assessedPopulation.subList(0, Math.min(POPULATION_SIZE, assessedPopulation.size()));
            System.out.println("generation " + generationNumber +
                    ",\tbest: " + assessedPopulation.get(0).getRight() +
                    ",\tmean: " + assessedPopulation.stream().mapToDouble(Pair::getRight).average().getAsDouble() +
                    ",\tworst: " + assessedPopulation.get(assessedPopulation.size() - 1).getRight() +
                    ",\tmutations: " + numberOfMutations);
        }
        return assessedPopulation.get(0).getLeft();
    }

    private List<Genotype> createNewGeneration(List<Pair<Genotype, Double>> assessedPopulation,
                                               List<Genotype> newGenotypes) {
        Stream<Genotype> oldies = assessedPopulation.stream().map(Pair::getLeft);
        return Stream.concat(oldies, newGenotypes.stream()).collect(toList());
    }

    private List<Pair<Genotype, Genotype>> pairParents(List<Genotype> parents) {
        List<Pair<Genotype, Genotype>> pairs = new ArrayList<>();
        List<Integer> indexes = IntStream.range(0, parents.size()).boxed().collect(toList());
        Collections.shuffle(indexes);
        for (int i = 0; i < indexes.size(); i += 2) {
            pairs.add(Pair.of(parents.get(i), parents.get(i + 1)));
        }
        return pairs;
    }

    private List<Genotype> chooseParents(List<Pair<Genotype, Double>> assessedPopulation) {
        List<Genotype> parents = new ArrayList<>();
        int populationSize = assessedPopulation.size();
        for (int i = 0; i < populationSize; i++) {
            double selectionRate = calculateSelectionRate(i, populationSize);
            double chanceOfBecomingAParent = calculateChanceOfBecomingAParent(selectionRate, populationSize);
            boolean willItBecomeAParent = calculateIfGenotypeWillBecomeAParent(chanceOfBecomingAParent);
            if (willItBecomeAParent) {
                parents.add(assessedPopulation.get(i).getLeft());
            }
        }
        if (parents.size() % 2 == 1) {
            parents.remove(parents.size() - 1);
        }
        return parents;
    }

    private boolean calculateIfGenotypeWillBecomeAParent(double chanceOfBecomingAParent) {
        return random.nextDouble() < chanceOfBecomingAParent;
    }

    private double calculateChanceOfBecomingAParent(double selectionRate, int populationSize) {
        return selectionRate / pow(populationSize, 1.5);
    }

    private double calculateSelectionRate(int genotypeRankingPosition, int populationSize) {
        return sqrt(pow(populationSize - genotypeRankingPosition, 3));
    }

    private List<Genotype> initializePopulation() {
        List<Genotype> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Genotype genotype = new Genotype(appSettings);
            genotype.fillRandomlyWithLessons();
            population.add(genotype);
        }
        return population;
    }
}
