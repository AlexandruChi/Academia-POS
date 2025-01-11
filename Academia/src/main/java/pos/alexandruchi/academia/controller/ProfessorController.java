package pos.alexandruchi.academia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.ProfessorDTO;
import pos.alexandruchi.academia.exception.authorization.Unauthenticated;
import pos.alexandruchi.academia.exception.authorization.Unauthorized;
import pos.alexandruchi.academia.mapper.Lecture.LectureMapper;
import pos.alexandruchi.academia.mapper.Professor.ProfessorMapper;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Professor;
import pos.alexandruchi.academia.service.AuthorizationService;
import pos.alexandruchi.academia.service.ProfessorService;
import pos.alexandruchi.academia.service.AuthorizationService.Role;
import pos.alexandruchi.academia.service.AuthorizationService.Claims;
import pos.alexandruchi.academia.converter.types.*;

import java.util.*;

@RestController
@RequestMapping(ProfessorController.path)
public class ProfessorController {
    @Value("${server.servlet.context-path}")
    public String context;

    public static final String path = "/professors";

    private final AuthorizationService authorizationService;
    private final ProfessorService professorService;
    private final ProfessorMapper professorMapper;
    private final LectureMapper lectureMapper;

    @Autowired
    public ProfessorController(
            AuthorizationService authorizationService, ProfessorService professorService,
            ProfessorMapper professorMapper, LectureMapper lectureMapper
    ) {
        this.authorizationService = authorizationService;
        this.professorService = professorService;
        this.professorMapper = professorMapper;
        this.lectureMapper = lectureMapper;
    }

    @GetMapping
    public List<Map<String, Object>> getProfessors(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "academic_rank", required = false) String academicRank,
            @RequestParam(value = "name", required = false) String lastNameStart
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        TeachingDegree teachingDegree = null;

        try {
            if (academicRank != null) {
                teachingDegree = TeachingDegree.of(academicRank);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        List<Map<String, Object>> professors = new ArrayList<>();
        for (Professor professor : professorService.getProfessors(lastNameStart, teachingDegree)) {
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
                    NumberFormatException::new
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

    @GetMapping("/{id}/lectures")
    public List<Map<String, Object>> getProfessorLectures(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(Role.ADMIN, Role.STUDENT));
        Professor professor;

        try {
            if (!Objects.equals((
                    professor = professorService.getProfessor(Integer.valueOf(id)).orElseThrow(
                            NumberFormatException::new
                    )
            ).getEmail(), claims.email())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<Map<String, Object>> lectures = new ArrayList<>();

        for (Lecture lecture : professorService.getLectures(professor)) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", lecture.getId());
            map.put("lecture", lectureMapper.toDTO(lecture));
            lectures.add(map);
        }

        return lectures;
    }

    /// Check if user has the required role and sends response appropriate code otherwise
    @SuppressWarnings("UnusedReturnValue")
    private Claims CheckAuthorization(String authorization, List<AuthorizationService.Role> roles) {
        try {
            return authorizationService.checkAuthorization(authorization, roles);
        } catch (Unauthenticated e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
