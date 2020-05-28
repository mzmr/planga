package pl.znamirowski.planga.generator;

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
        for (Genotype genotype : population) {
            if (random.nextDouble() < MUTATION_PROBABILITY) {
                mutate(genotype);
            }
        }
    }

    private void mutate(Genotype genotype) {
        List<LessonTuple> lessons = settings.getLessonTuples();
        int lessonToMove = random.nextInt(lessons.size());
        int oldLessonStartIndex = genotype.deleteLesson(lessonToMove);
        List<Integer> validPositionsForLesson = genotype.findValidPositionsForLesson(lessons.get(lessonToMove));
        validPositionsForLesson.remove(Integer.valueOf(oldLessonStartIndex));
        int newLessonStartIndex = validPositionsForLesson.get(random.nextInt(validPositionsForLesson.size()));
        genotype.writeLessonAt(newLessonStartIndex, lessonToMove);
    }
}
