package pos.alexandruchi.academia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.model.Professor;
import pos.alexandruchi.academia.service.ProfessorService;

@RestController
@RequestMapping("/professor")
public class ProfessorController {
    private final ProfessorService professorService;

    @Autowired
    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping("/{id}")
    public Professor getProfessor(@PathVariable String id) {
        Professor professor;
        try {
            professor = professorService.getProfessor(Integer.valueOf(id));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id not a number");
        }

        if (professor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return professor;
    }

    @PostMapping
    public Professor addProfessor(@RequestBody Professor professor) {
        Professor newProfessor = professorService.setProfessor(professor);

        if (newProfessor == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        return newProfessor;
    }

    @DeleteMapping("/{id}")
    public void deleteProfessor(@PathVariable String id) {
        try {
            professorService.deleteProfessor(
                    professorService.getProfessor(Integer.valueOf(id))
            );
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
