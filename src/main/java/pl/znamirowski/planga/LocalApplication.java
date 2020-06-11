package pl.znamirowski.planga;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import pl.znamirowski.planga.generator.GeneratorRunner;
import pl.znamirowski.planga.generator.genetic.Genotype;
import pl.znamirowski.planga.service.response.TimetableResponse;
import pl.znamirowski.planga.service.response.TimetableResponseBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalApplication {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("Exactly 1 argument have to be passed.");
        }
        String workspaceFolder = args[0];
        prepareOutputFolder(workspaceFolder);

        Genotype genotype = new GeneratorRunner().runTimetableGenerator();
        TimetableResponse response = new TimetableResponseBuilder().buildResponse(genotype);
        writeResponseToFile(workspaceFolder, response);
    }

    private static void prepareOutputFolder(String workspaceFolder) {
        File outputFolder = new File(workspaceFolder + "/timetables");
        if (outputFolder.isDirectory()) {
            return;
        }
        if (!outputFolder.mkdir()) {
            throw new RuntimeException("Couldn't create output folder.");
        }
    }

    private static void writeResponseToFile(String workspaceFolder, TimetableResponse response) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalTime.class,
                (JsonSerializer<LocalTime>) (localTime, type, jsonSerializationContext) ->
                    new JsonPrimitive(localTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        ).create();
        FileWriter fileWriter = new FileWriter(workspaceFolder +"/timetables/timetable-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss")) + ".json");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(gson.toJson(response));
        printWriter.close();
        fileWriter.close();
    }
}
