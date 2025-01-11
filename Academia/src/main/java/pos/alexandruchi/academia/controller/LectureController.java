package pos.alexandruchi.academia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.LectureDTO;
import pos.alexandruchi.academia.exception.authorization.Unauthenticated;
import pos.alexandruchi.academia.exception.authorization.Unauthorized;
import pos.alexandruchi.academia.mapper.Lecture.LectureMapper;
import pos.alexandruchi.academia.mapper.Student.StudentMapper;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.service.*;
import pos.alexandruchi.academia.service.AuthorizationService.Role;
import pos.alexandruchi.academia.service.AuthorizationService.Claims;
import org.springframework.data.domain.Pageable;
import pos.alexandruchi.academia.converter.types.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(LectureController.path)
public class LectureController {
    private final StudentMapper studentMapper;
    @Value("${server.servlet.context-path}")
    public String context;

    public static final String path = "/lectures";

    private final LectureService lectureService;
    private final LectureMapper lectureMapper;
    private final EnrollService enrollService;
    private final AuthorizationService authorizationService;

    @Autowired
    public LectureController(
            LectureService lectureService, LectureMapper lectureMapper,
            EnrollService enrollService, AuthorizationService authorizationService,
            StudentMapper studentMapper) {
        this.lectureService = lectureService;
        this.lectureMapper = lectureMapper;
        this.enrollService = enrollService;
        this.authorizationService = authorizationService;
        this.studentMapper = studentMapper;
    }

    @GetMapping
    public ObjectNode getLectures(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String type, @RequestParam(required = false) String category,
            @RequestParam(value = "page_items", defaultValue = "10") String pageItems,
            @RequestParam(required = false) String page, HttpServletRequest request
            ) {
        Claims claims = CheckAuthorization(authorization, List.of(
                Role.ADMIN, Role.PROFESSOR, Role.STUDENT
        ));

        /* Request parameters */

        Pageable pageable = Pageable.unpaged();
        int pageNumber = -1;
        if (page != null) {
            try {
                pageNumber = Integer.parseInt(page);
                pageable = PageRequest.of(pageNumber, Integer.parseInt(pageItems));
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        LectureType lectureType = null;
        LectureCategory lectureCategory = null;

        try {
            if (type != null) {
                lectureType = LectureType.of(type);
            }

            if (category != null) {
                lectureCategory = LectureCategory.of(category);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        /* Lecture list */

        Page<Lecture> lecturesPage = lectureService.getLectures(pageable, lectureType, lectureCategory);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Lecture lecture : lecturesPage) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", lecture.getId());
            map.put("lecture", lectureMapper.toDTO(lecture));
            list.add(map);
        }

        if (list.isEmpty() && (pageNumber != -1)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        /* Response */

        String URI = request.getRequestURI();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode lecturesJSON = objectMapper.createObjectNode();
        lecturesJSON.set("list", objectMapper.valueToTree(list));

        Map<String, List<Object>> query = new HashMap<>();
        query.put("type", Stream.of(
                LectureType.values()).map(LectureType::toString).collect(Collectors.toList()
        ));
        query.put("category", Stream.of(
                LectureCategory.values()).map(LectureCategory::toString).collect(Collectors.toList()
        ));

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, query));

        if (lecturesPage.hasNext()) {
            links.put("next", createLink(
                    URL.replaceAll("page=" + pageNumber, "page=" + (pageNumber + 1)),
                    "GET", null
            ));
        }

        if (lecturesPage.hasPrevious()) {
            links.put("prev", createLink(
                    URL.replaceAll("page=" + pageNumber, "page=" + (pageNumber - 1)),
                    "GET", null
            ));
        }

        links.put("lecture", createLink(URI + "/{code}", "GET", null));
        links.put("professor", createLink(
                context + ProfessorController.path + "/{idHolder}", "GET", null
        ));

        if (List.of(Role.PROFESSOR, Role.ADMIN).contains(claims.role())) {
            links.put("students", createLink(URI + "{code}/students", "GET", null));
        }

        lecturesJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("lectures", lecturesJSON);

        return ret;
    }

    @GetMapping("/{code}")
    public ObjectNode getLecture(
            @PathVariable String code, @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(
                Role.ADMIN, Role.PROFESSOR, Role.STUDENT
        ));

        /* Lecture */

        Lecture lecture = lectureService.getLecture(code).orElse(null);

        if (!CheckAcces(claims, lecture)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        LectureDTO dto = lectureMapper.toDTO(lecture);

        /* Response */

        String URI = request.getRequestURI();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode lectureJSON = objectMapper.valueToTree(dto);

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, null));
        links.put("professor", createLink(
                context + ProfessorController.path + "/" + dto.idHolder,
                "GET", null
        ));

        if (List.of(Role.PROFESSOR, Role.ADMIN).contains(claims.role())) {
            links.put("students", createLink(URI + "/students", "GET", null));
        }

        if (claims.role() == Role.ADMIN) {
            links.put("modify", createLink(URI, "PUT", null));
            links.put("delete", createLink(URI, "DELETE", null));
        }

        lectureJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("lecture", lectureJSON);

        return ret;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{code}", consumes = "application/json")
    public void setLecture(
            @PathVariable String code, @RequestBody(required = false) LectureDTO lectureDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN ));
        Lecture lecture = lectureService.getLecture(code).orElse(null);

        try {
            if (lecture == null) {
                lecture = lectureMapper.toEntity(lectureDTO, code);
            } else {
                lectureMapper.setEntity(lecture, lectureDTO);
            }

            if (lecture == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }

            lectureService.setLecture(lecture);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{code}")
    public void deleteLecture(
            @PathVariable String code, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));
        lectureService.deleteLecture(lectureService.getLecture(code).orElse(null));
    }

    @GetMapping("{code}/students")
    public ObjectNode getStudents(
            @PathVariable String code, @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(
                Role.PROFESSOR, Role.ADMIN
        ));

        /* Students */

        List<Map<String, Object>> list = new ArrayList<>();
        for (Student student : lectureService.getStudents(
                lectureService.getLecture(code).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
                )
        )) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", student.getId());
            map.put("student", studentMapper.toDTO(student));
            list.add(map);
        }

        /* Response */

        String URI = request.getRequestURI();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode studentsJSON = objectMapper.createObjectNode();
        studentsJSON.set("list", objectMapper.valueToTree(list));

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, null));

        if (!list.isEmpty()) {
            links.put("student", createLink(
                    context + StudentController.path + "/{id}", "GET", null
            ));
        }

        if (claims.role() == Role.ADMIN) {
            links.put("add_student", createLink(
                    context + StudentController.path + "/{id}/lectures?code=" + code,
                    "POST", null
            ));

            if (!list.isEmpty()) {
                links.put("remove_student", createLink(
                        context + StudentController.path + "/{id}/lectures/" + code,
                        "DELETE", null
                ));
            }
        }

        studentsJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("students", studentsJSON);

        return ret;
    }

    private Map<String, Object> createLink(String link, String method, Map<String, List<Object>> query) {
        if (link == null) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("href", link);

        if (method != null) {
            map.put("type", method);
        }

        if (query != null) {
            map.put("query", query);
        }

        return map;
    }

    /// Check if user has the required role and sends response appropriate code otherwise
    private Claims CheckAuthorization(String authorization, List<Role> roles) {
        try {
            return authorizationService.checkAuthorization(authorization, roles);
        } catch (Unauthenticated e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    /// Check if a user has access to a lecture
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean CheckAcces(Claims claims, Lecture lecture) {
        return claims.role() != Role.STUDENT || enrollService.isEnrolled(
                (Student) authorizationService.getEntity(claims), lecture);
    }
}
