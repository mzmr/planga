package pl.znamirowski.planga.generator;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CrossoverPerformer {

    public CrossoverPerformer(AppSettings settings) {

    }

    public List<Genotype> performCrossover(List<Pair<Genotype, Genotype>> pairs) {
        return pairs.stream().map(Pair::getLeft).collect(toList());
    }
}
