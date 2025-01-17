package pos.alexandruchi.academia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.StudentDTO;
import pos.alexandruchi.academia.exception.authorization.Unauthenticated;
import pos.alexandruchi.academia.exception.authorization.Unauthorized;
import pos.alexandruchi.academia.mapper.Lecture.LectureMapper;
import pos.alexandruchi.academia.mapper.Student.StudentMapper;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.service.AuthorizationService;
import pos.alexandruchi.academia.service.EnrollService;
import pos.alexandruchi.academia.service.LectureService;
import pos.alexandruchi.academia.service.StudentService;
import pos.alexandruchi.academia.service.AuthorizationService.Role;
import pos.alexandruchi.academia.service.AuthorizationService.Claims;
import pos.alexandruchi.academia.types.TeachingDegree;

import static pos.alexandruchi.academia.utilclass.LinkUtil.createLink;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(StudentController.path)
public class StudentController {
    @Value("${server.servlet.context-path}")
    public String context;

    public static final String path = "/students";

    private final AuthorizationService authorizationService;
    private final StudentService studentService;
    private final StudentMapper studentMapper;
    private final LectureMapper lectureMapper;
    private final EnrollService enrollService;
    private final LectureService lectureService;

    @Autowired
    public StudentController(
            AuthorizationService authorizationService, StudentService studentService,
            StudentMapper studentMapper, LectureMapper lectureMapper, EnrollService enrollService,
            LectureService lectureService) {
        this.authorizationService = authorizationService;
        this.studentService = studentService;
        this.studentMapper = studentMapper;
        this.lectureMapper = lectureMapper;
        this.enrollService = enrollService;
        this.lectureService = lectureService;
    }

    @GetMapping()
    public ObjectNode getStudents(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String email, HttpServletRequest request
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(Role.ADMIN, Role.SERVICE, Role.PROFESSOR));

        /* Student list */

        List<Map<String, Object>> list = new ArrayList<>();
        for (Student student : studentService.getStudents(email)) {
            Map<String, Object> map = new LinkedHashMap<>();
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

        if (email != null) {
            studentsJSON.put("email", email);
        }

        studentsJSON.set("list", objectMapper.valueToTree(list));

        Map<String, Object> query = new HashMap<>();
        query.put("email", "unique student email");

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, query));

        if (!list.isEmpty()) {
            links.put("student", createLink(URI + "/{id}", "GET", null));
            links.put("lectures", createLink(URI + "/{id}/lectures", "GET", null));
        }

        if (claims.role() == Role.ADMIN) {
            links.put("create", createLink(URI, "POST", null));
            if (!list.isEmpty()) {
                links.put("delete", createLink(URI + "/{id}", "DELETE", null));
            }
        }

        studentsJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("students", studentsJSON);

        return ret;
    }

    @GetMapping("/{id}")
    public ObjectNode getStudent(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(
                Role.ADMIN, Role.SERVICE, Role.STUDENT, Role.PROFESSOR)
        );

        Student student;

        /* Student */

        try {
            student = studentService.getStudent(Integer.valueOf(id)).orElseThrow(NumberFormatException::new);
            if (claims.role() == Role.STUDENT && !Objects.equals(student.getEmail(), claims.email())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        StudentDTO dto = studentMapper.toDTO(student);

        /* Response */

        String URI = request.getRequestURI();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode studentsJSON = objectMapper.valueToTree(dto);

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, null));
        links.put("lectures", createLink(URI + "/lectures", "GET", null));

        if (claims.role() == Role.ADMIN) {
            links.put("enroll", createLink(URI + "/lectures?code={lecture}", "POST", null));
            links.put("delete", createLink(URI, "DELETE", null));
        }

        studentsJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("student", studentsJSON);

        return ret;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json")
    public ObjectNode addStudent(
            @RequestBody(required = false) StudentDTO studentDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        /* Create Student */

        Student student = studentService.setStudent(
                studentMapper.toEntity(studentDTO)
        );

        if (student == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        StudentDTO dto = studentMapper.toDTO(student);

        /* Response */

        String URI = request.getRequestURI() + "/" + student.getId();
        String queryParameters = request.getQueryString();
        String URL = URI + ((queryParameters != null) ? ("?" + queryParameters) : "");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode studentsJSON = objectMapper.valueToTree(dto);

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, null));
        links.put("lectures", createLink(URI + "/lectures", "GET", null));
        links.put("enroll", createLink(URI + "/lectures?code={lecture}", "POST", null));
        links.put("delete", createLink(URI, "DELETE", null));

        studentsJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.put("id", student.getId());
        ret.set("student", studentsJSON);

        return ret;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteStudent(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        try {
            studentService.deleteStudent(
                    studentService.getStudent(Integer.valueOf(id)).orElse(null)
            );
        } catch (NumberFormatException ignored) {}
    }

    @GetMapping("/{id}/lectures")
    public ObjectNode getStudentLectures(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(Role.ADMIN, Role.SERVICE, Role.STUDENT));
        Student student;

        /* Lectures */

        try {
            student = studentService.getStudent(Integer.valueOf(id)).orElseThrow(NumberFormatException::new);
            if (claims.role() == Role.STUDENT && !Objects.equals(student.getEmail(), claims.email())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Lecture lecture : studentService.getLectures(student)) {
            Map<String, Object> map = new HashMap<>();
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
        lecturesJSON.put("student", student.getId());
        lecturesJSON.set("list", objectMapper.valueToTree(list));

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink(URL, null, null));

        if (!list.isEmpty()) {
            links.put("lecture", createLink(
                    context + LectureController.path + "/{code}", "GET", null
            ));
        }

        if (claims.role() == Role.ADMIN) {
            links.put("add_lecture", createLink(URL + "?code={code}", "POST", null
            ));

            if (!list.isEmpty()) {
                links.put("remove_lecture", createLink(URL + "/{code}", "DELETE", null
                ));
            }
        }

        lecturesJSON.set("_links", objectMapper.valueToTree(links));

        ObjectNode ret = objectMapper.createObjectNode();
        ret.set("lectures", lecturesJSON);

        return ret;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id}/lectures")
    public void addStudentLecture(
            @PathVariable String id, @RequestParam(required = false) String code,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        try {
            if (!enrollService.enroll(
                    studentService.getStudent(Integer.valueOf(id)).orElseThrow(NumberFormatException::new),
                    lectureService.getLecture(code).orElseThrow(IllegalArgumentException::new)
            )) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/lectures/{code}")
    public void deleteStudentLecture(
            @PathVariable String id, @PathVariable String code,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        try {
            enrollService.unenroll(
                    studentService.getStudent(Integer.valueOf(id)).orElseThrow(NumberFormatException::new),
                    lectureService.getLecture(code).orElseThrow(IllegalArgumentException::new)
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
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
