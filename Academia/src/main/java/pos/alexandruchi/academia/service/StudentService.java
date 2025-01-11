package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.JoinDS;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.repository.JoinDSRepository;
import pos.alexandruchi.academia.repository.StudentRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final JoinDSRepository joinDSRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, JoinDSRepository joinDSRepository) {
        this.studentRepository = studentRepository;
        this.joinDSRepository = joinDSRepository;
    }

    public Iterable<Student> getStudents() {
        return getStudents(Pageable.unpaged());
    }

    public Iterable<Student> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Optional<Student> getStudent(Integer id) {
        return studentRepository.findById(id);
    }

    public Student setStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException();
        }

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

    public Iterable<Lecture> getLectures(Student student) {
        return StreamSupport.stream(joinDSRepository.findAllByStudentID(student).spliterator(), false)
                .map(JoinDS::getLectureID).toList();
    }
}
