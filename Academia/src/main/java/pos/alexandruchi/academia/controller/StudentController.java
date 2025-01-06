package pos.alexandruchi.academia.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.StudentDTO;
import pos.alexandruchi.academia.exception.authorization.Unauthenticated;
import pos.alexandruchi.academia.exception.authorization.Unauthorized;
import pos.alexandruchi.academia.mapper.Student.StudentMapper;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.service.AuthorizationService;
import pos.alexandruchi.academia.service.StudentService;
import pos.alexandruchi.academia.service.AuthorizationService.Role;
import pos.alexandruchi.academia.service.AuthorizationService.Claims;

import java.util.*;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final AuthorizationService authorizationService;
    private final StudentService studentService;
    private final StudentMapper studentMapper;

    @Autowired
    public StudentController(AuthorizationService authorizationService, StudentService studentService, StudentMapper studentMapper) {
        this.authorizationService = authorizationService;
        this.studentService = studentService;
        this.studentMapper = studentMapper;
    }

    @GetMapping()
    public List<Map<String, Object>> getStudents(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        List<Map<String, Object>> students = new ArrayList<>();

        for (Student student : studentService.getStudents()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", student.getId());
            map.put("student", studentMapper.toDTO(student));
            students.add(map);
        }

        return students;
    }

    @GetMapping("/{id}")
    public StudentDTO getStudent(
            @PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));

        try {
            return studentMapper.toDTO(studentService.getStudent(Integer.valueOf(id)).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
            ));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json")
    public StudentDTO addStudent(
            @RequestBody(required = false) StudentDTO studentDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));
        Student student = studentService.setStudent(
                studentMapper.toEntity(studentDTO)
        );

        if (student == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        return studentMapper.toDTO(student);
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
