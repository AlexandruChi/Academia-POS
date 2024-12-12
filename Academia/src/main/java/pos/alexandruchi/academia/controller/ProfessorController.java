package pos.alexandruchi.academia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.ProfessorDTO;
import pos.alexandruchi.academia.mapper.Professor.ProfessorMapper;
import pos.alexandruchi.academia.model.Professor;
import pos.alexandruchi.academia.service.ProfessorService;

@RestController
@RequestMapping("/professor")
public class ProfessorController {
    private final ProfessorService professorService;
    private final ProfessorMapper professorMapper;

    @Autowired
    public ProfessorController(ProfessorService professorService, ProfessorMapper professorMapper) {
        this.professorService = professorService;
        this.professorMapper = professorMapper;
    }

    @GetMapping("/{id}")
    public ProfessorDTO getProfessor(@PathVariable String id) {
        Professor professor;

        try {
            professor = professorService.getProfessor(Integer.valueOf(id));
        } catch (NumberFormatException e) {
            professor = null;
        }

        if (professor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return professorMapper.toDTO(professor);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessorDTO addProfessor(@RequestBody ProfessorDTO professorDTO) {
        Professor professor = professorService.addProfessor(
                professorMapper.toEntity(professorDTO)
        );

        if (professor == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        return professorMapper.toDTO(professor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfessor(@PathVariable String id) {
        try {
            professorService.deleteProfessor(
                    professorService.getProfessor(Integer.valueOf(id))
            );
        } catch (NumberFormatException ignored) {}
    }
}
