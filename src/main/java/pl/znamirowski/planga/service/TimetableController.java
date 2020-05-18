package pl.znamirowski.planga.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.znamirowski.planga.generator.GeneratorRunner;
import pl.znamirowski.planga.generator.Genotype;

import java.io.IOException;

@Controller
@RequestMapping("/timetable")
public class TimetableController {
    private final GeneratorRunner generatorRunner;

    @Autowired
    public TimetableController(GeneratorRunner generatorRunner) {
        this.generatorRunner = generatorRunner;
    }

    @GetMapping("/generate")
    public ResponseEntity<TimetableResponse> generateTimetable() throws IOException {
        Genotype genotype = generatorRunner.runTimetableGenerator();
        TimetableResponse response = new TimetableResponseBuilder().buildResponse(genotype);
        return ResponseEntity.ok(response);
    }
}
