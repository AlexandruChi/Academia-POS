package pos.alexandruchi.academia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
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
import pos.alexandruchi.academia.types.LectureCategory;
import pos.alexandruchi.academia.types.LectureType;
import pos.alexandruchi.academia.types.TeachingDegree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pos.alexandruchi.academia.utilclass.LinkUtil.createLink;

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
    public ObjectNode getProfessors(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "academic_rank", required = false) String academicRank,
            @RequestParam(value = "name", required = false) String lastNameStart,
            HttpServletRequest request
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(
                Role.STUDENT, Role.PROFESSOR, Role.ADMIN
        ));

        /* Request parameters */

        TeachingDegree teachingDegree = null;

        try {
            if (academicRank != null) {
                teachingDegree = TeachingDegree.of(academicRank);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        /* Professor List */

        List<Map<String, Object>> list = new ArrayList<>();
        for (Professor professor : professorService.getProfessors(lastNameStart, teachingDegree)) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", professor.getId());
            map.put("professor", professorMapper.toDTO(professor));
            list.add(map);
        }

        /* Response */

        String URI = request.getRequestURI();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode professorsJSON = objectMapper.createObjectNode();

        if (teachingDegree != null) {
            professorsJSON.put("academic_rank", teachingDegree.toString());
        }

        if (lastNameStart != null) {
            professorsJSON.put("name", lastNameStart);
        }

        professorsJSON.set("list", objectMapper.valueToTree(list));

        Map<String, Object> query = new HashMap<>();
        query.put("academic_rank", Stream.of(
                TeachingDegree.values()).map(TeachingDegree::toString).collect(Collectors.toList()
        ));

        query.put("name", "name beginning");

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, query));

        links.put("professor", createLink(URI + "/{id}", "GET", null));
        links.put("lectures", createLink(URI + "/{id}/lectures", "GET", null));

        if (claims.role() == Role.ADMIN) {
            links.put("create", createLink(URI, "POST", null));
            if (!list.isEmpty()) {
                links.put("delete", createLink(URI + "/{id}", "DELETE", null));
            }
        }

        professorsJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("professors", professorsJSON);

        return ret;
    }

    @GetMapping("/{id}")
    public ObjectNode getProfessor(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(Role.ADMIN, Role.PROFESSOR, Role.STUDENT));
        ProfessorDTO dto;

        /* Professor */

        try {
            dto = professorMapper.toDTO(professorService.getProfessor(Integer.valueOf(id)).orElseThrow(
                    NumberFormatException::new
            ));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        /* Response */

        String URI = request.getRequestURI();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode professorJSON = objectMapper.valueToTree(dto);

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, null));
        links.put("lectures", createLink(URI + "/lectures", "GET", null));

        if (claims.role() == Role.ADMIN) {
            links.put("delete", createLink(URI, "DELETE", null));
        }

        professorJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("student", professorJSON);

        return ret;
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
    public ObjectNode getProfessorLectures(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        CheckAuthorization(authorization, List.of(Role.PROFESSOR, Role.ADMIN, Role.STUDENT));
        Professor professor;

        /* Professor */

        try {
            professor = professorService.getProfessor(Integer.valueOf(id)).orElseThrow(NumberFormatException::new);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Lecture lecture : professorService.getLectures(professor)) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("code", lecture.getId());
            map.put("lecture", lectureMapper.toDTO(lecture));
            list.add(map);
        }

        /* Response */

        String URI = request.getRequestURI();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode lecturesJSON = objectMapper.createObjectNode();
        lecturesJSON.put("professor", professor.getId());
        lecturesJSON.set("list", objectMapper.valueToTree(list));

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, null));

        if (!list.isEmpty()) {
            links.put("lecture", createLink(
                    context + LectureController.path + "/{code}", "GET", null
            ));
        }

        lecturesJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("lectures", lecturesJSON);

        return ret;
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
