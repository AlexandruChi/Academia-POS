package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.repository.StudentRepository;

import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Iterable<Student> getStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudent(Integer id) {
        return studentRepository.findById(id);
    }

    public Student setStudent(Student student) {
        try {
            return studentRepository.save(student);
        } catch (DataIntegrityViolationException e) {
            return null;
        }
    }

    public void deleteStudent(Student student) {
        try {
            studentRepository.delete(student);
        } catch (Exception ignores) {}
    }
}
