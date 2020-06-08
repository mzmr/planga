package pl.znamirowski.planga.generator.genetic.fitness;

import org.apache.commons.lang3.tuple.Pair;
import pl.znamirowski.planga.generator.genetic.Genotype;
import pl.znamirowski.planga.generator.settings.AppSettings;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Comparator.comparingDouble;

public class GenotypeAssessor {
    /*

    Group breaks - groups should have 15 minutes (or 0 minutes) break between lessons
    Group starting hour - groups should start lessons at 8am
    Group regular lessons - groups should have lessons placed regularly in all days in a week

    Teacher breaks - teachers should have 15 minutes (or 0 minutes) break between lessons
    Teacher lessons a day - teachers should not exceed the maximum number of lessons allowed per day

    */

    private final AppSettings appSettings;
    private final int bestTimeWindowToStart;
    private ExecutorService threadPool;

    public GenotypeAssessor(AppSettings appSettings) {
        this.appSettings = appSettings;
        this.bestTimeWindowToStart = (int) MINUTES.between(LocalTime.parse("08:00"), appSettings.getStartHour())
                / appSettings.getMinutesPerTimeWindow();
        this.threadPool = Executors.newFixedThreadPool(8);
    }

    public List<Pair<Genotype, Double>> assessPopulation(List<Genotype> population) {
        Map<Genotype, Future<Double>> futures = new HashMap<>();
        for (Genotype genotype : population) {
            Future<Double> future = threadPool.submit(new AssessThread(appSettings, genotype, bestTimeWindowToStart));
            futures.put(genotype, future);
        }

        List<Pair<Genotype, Double>> assessedPopulation = new ArrayList<>();
        for (Map.Entry<Genotype, Future<Double>> entry : futures.entrySet()) {
            try {
                Double assessment = entry.getValue().get();
                assessedPopulation.add(Pair.of(entry.getKey(), assessment));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        assessedPopulation.sort(comparingDouble(Pair::getRight));
        return assessedPopulation;
    }


}
