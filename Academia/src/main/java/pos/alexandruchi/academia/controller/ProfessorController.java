package pos.alexandruchi.academia.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.ProfessorDTO;
import pos.alexandruchi.academia.exception.authorization.Unauthenticated;
import pos.alexandruchi.academia.exception.authorization.Unauthorized;
import pos.alexandruchi.academia.mapper.Professor.ProfessorMapper;
import pos.alexandruchi.academia.model.Professor;
import pos.alexandruchi.academia.service.AuthorizationService;
import pos.alexandruchi.academia.service.ProfessorService;
import pos.alexandruchi.academia.service.AuthorizationService.Role;
import pos.alexandruchi.academia.service.AuthorizationService.Claims;

import java.util.*;

@RestController
@RequestMapping("/professors")
public class ProfessorController {
    private final AuthorizationService authorizationService;
    private final ProfessorService professorService;
    private final ProfessorMapper professorMapper;

    @Autowired
    public ProfessorController(AuthorizationService authorizationService, ProfessorService professorService, ProfessorMapper professorMapper) {
        this.authorizationService = authorizationService;
        this.professorService = professorService;
        this.professorMapper = professorMapper;
    }

    @GetMapping
    public List<Map<String, Object>> getProfessors(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        List<Map<String, Object>> professors = new ArrayList<>();

        for (Professor professor : professorService.getProfessors()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", professor.getId());
            map.put("professor", professorMapper.toDTO(professor));
            professors.add(map);
        }

        return professors;
    }

    @GetMapping("/{id}")
    public ProfessorDTO getProfessor(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        try {
            return professorMapper.toDTO(professorService.getProfessor(Integer.valueOf(id)).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
            ));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json")
    public ProfessorDTO addProfessor(
            @RequestBody(required = false) ProfessorDTO professorDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));
        Professor professor = professorService.setProfessor(
                professorMapper.toEntity(professorDTO)
        );

        if (professor == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        return professorMapper.toDTO(professor);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteProfessor(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        try {
            professorService.deleteProfessor(
                    professorService.getProfessor(Integer.valueOf(id)).orElse(null)
            );
        } catch (NumberFormatException ignored) {}
    }

    /// Check if user has the required role and sends response appropriate code otherwise
    @SuppressWarnings("UnusedReturnValue")
    private @NotNull Claims CheckAuthorization(String authorization, List<AuthorizationService.Role> roles) {
        try {
            return authorizationService.checkAuthorization(authorization, roles);
        } catch (Unauthenticated e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
