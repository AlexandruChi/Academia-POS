package pos.alexandruchi.academia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.service.StudentService;

@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable String id) {
        Student student;
        try {
            student = studentService.getStudent(Integer.valueOf(id));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return student;
    }

    @PostMapping
    public Student addStudent(@RequestBody Student student) {
        Student newStudent = studentService.setStudent(student);

        if (newStudent == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        return newStudent;
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable String id) {
        try {
            studentService.deleteStudent(
                    studentService.getStudent(Integer.valueOf(id))
            );
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
