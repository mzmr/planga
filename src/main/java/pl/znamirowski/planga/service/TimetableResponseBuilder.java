package pl.znamirowski.planga.service;

import pl.znamirowski.planga.generator.AppSettings;
import pl.znamirowski.planga.generator.Genotype;
import pl.znamirowski.planga.generator.LessonTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimetableResponseBuilder {

    public TimetableResponse buildResponse(Genotype genotype) {
        AppSettings settings = genotype.getSettings();
        Map<Integer, LessonTuple> lessonTuples = settings.getLessonTuples();
        List<Lesson> lessons = new ArrayList<>(lessonTuples.size());
        for (int dayNumber = 0; dayNumber < settings.getDaysPerWeek(); dayNumber++) {
            for (int roomNumber = 0; roomNumber < settings.getNumberOfRooms(); roomNumber++) {
                int previousLessonId = -1;

                for (int windowNumber = 0; windowNumber < settings.getTimeWindowsPerDay(); windowNumber++) {
                    int lessonId = genotype.getLessonIdAt(dayNumber, windowNumber, roomNumber);
                    if (lessonId != -1 && previousLessonId != lessonId) {
                        LessonTuple lessonTuple = lessonTuples.get(lessonId);
                        Lesson lesson = new Lesson(lessonTuple, dayNumber, windowNumber, roomNumber, settings);
                        lessons.add(lesson);
                    }
                    previousLessonId = lessonId;
                }
            }
        }
        return new TimetableResponse(settings, lessons);
    }
}
