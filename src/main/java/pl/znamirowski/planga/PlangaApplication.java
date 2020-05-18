package pl.znamirowski.planga;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.Files.readAllBytes;

public class PlangaApplication {

    public static void main(String[] args) throws IOException {
        var app = new PlangaApplication();
        var fileContent = app.readFile("inputSettings.json");
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalTime.class,
                (JsonDeserializer<LocalTime>) (json, type, jsonDeserializationContext) ->
                        LocalTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern("HH:mm"))).create();

        var inputSettings = gson.fromJson(fileContent, InputSettings.class);
        new TimetableGenerator(inputSettings).generateTimetable();
    }

    private String readFile(String fileName) throws IOException {
        return new String(readAllBytes(new File("src/main/resources/" + fileName).toPath()));
    }
}
