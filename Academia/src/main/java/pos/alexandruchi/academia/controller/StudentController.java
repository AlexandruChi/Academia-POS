package pos.alexandruchi.academia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.StudentDTO;
import pos.alexandruchi.academia.mapper.Student.StudentMapper;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.service.StudentService;

@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final StudentMapper studentMapper;

    @Autowired
    public StudentController(StudentService studentService, StudentMapper studentMapper) {
        this.studentService = studentService;
        this.studentMapper = studentMapper;
    }

    @GetMapping("/{id}")
    public StudentDTO getStudent(@PathVariable String id) {
        Student student;

        try {
            student = studentService.getStudent(Integer.valueOf(id));
        } catch (NumberFormatException e) {
            student = null;
        }

        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return studentMapper.toDTO(student);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDTO addStudent(@RequestBody StudentDTO studentDTO) {
        Student student = studentService.setStudent(
                studentMapper.toEntity(studentDTO)
        );

        if (student == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        return studentMapper.toDTO(student);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable String id) {
        try {
            studentService.deleteStudent(
                    studentService.getStudent(Integer.valueOf(id))
            );
        } catch (NumberFormatException ignored) {}
    }
}
