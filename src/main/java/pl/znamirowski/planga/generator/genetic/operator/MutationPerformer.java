package pl.znamirowski.planga.generator.genetic.operator;

import pl.znamirowski.planga.generator.genetic.Genotype;
import pl.znamirowski.planga.generator.settings.LessonTuple;
import pl.znamirowski.planga.generator.settings.AppSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MutationPerformer {
    private static final double MUTATION_PROBABILITY = 0.02;
    private final AppSettings settings;
    private final Random random;

    public MutationPerformer(AppSettings settings) {
        this.settings = settings;
        this.random = new Random();
    }

    public void performMutation(List<Genotype> population) {
        for (Genotype genotype : population.subList(1, population.size())) {
            if (random.nextDouble() < MUTATION_PROBABILITY) {
                mutate(genotype);
            }
        }
    }

    private void mutate(Genotype genotype) {
        List<LessonTuple> lessons = new ArrayList<>(settings.getLessonTuples().values());
        LessonTuple lessonToMove = lessons.get(random.nextInt(lessons.size()));
        int oldLessonStartIndex = genotype.deleteLesson(lessonToMove.getId());
        List<Integer> validPositionsForLesson = genotype.findValidPositionsForLesson(lessonToMove);
        validPositionsForLesson.remove(Integer.valueOf(oldLessonStartIndex));
        int newLessonStartIndex = validPositionsForLesson.get(random.nextInt(validPositionsForLesson.size()));
        genotype.writeLessonAt(newLessonStartIndex, lessonToMove);
    }
}
