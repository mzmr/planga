package pl.znamirowski.planga.service;

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
    private final GeneratorRunner generatorRunner = new GeneratorRunner();

    @GetMapping("/generate")
    public ResponseEntity<Genotype> generateTimetable() throws IOException {
        return ResponseEntity.ok(generatorRunner.runTimetableGenerator());
    }
}
