package pl.znamirowski.planga.service;

import pl.znamirowski.planga.generator.AppSettings;
import pl.znamirowski.planga.generator.Genotype;
import pl.znamirowski.planga.generator.LessonTuple;

import java.util.ArrayList;
import java.util.List;

public class TimetableResponseBuilder {

    public TimetableResponse buildResponse(Genotype genotype) {
        AppSettings settings = genotype.getSettings();
        List<LessonTuple> lessonTuples = settings.getLessonTuples();
        List<Lesson> lessons = new ArrayList<>(lessonTuples.size());
        for (int dayNumber = 0; dayNumber < settings.getDaysPerWeek(); dayNumber++) {
            for (int roomNumber = 0; roomNumber < settings.getNumberOfRooms(); roomNumber++) {
                int previousLessonIndex = -1;

                for (int windowNumber = 0; windowNumber < settings.getTimeWindowsPerDay(); windowNumber++) {
                    int lessonIndex = genotype.getLessonIndexAt(dayNumber, windowNumber, roomNumber);
                    if (lessonIndex != -1 && previousLessonIndex != lessonIndex) {
                        LessonTuple lessonTuple = lessonTuples.get(lessonIndex);
                        Lesson lesson = new Lesson(lessonTuple, dayNumber, windowNumber, roomNumber, settings);
                        lessons.add(lesson);
                    }
                    previousLessonIndex = lessonIndex;
                }
            }
        }
        return new TimetableResponse(settings, lessons);
    }
}
