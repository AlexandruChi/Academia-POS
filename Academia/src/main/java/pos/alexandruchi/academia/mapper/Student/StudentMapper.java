package pos.alexandruchi.academia.mapper.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pos.alexandruchi.academia.DTO.StudentDTO;
import pos.alexandruchi.academia.model.Student;

@Component
public class StudentMapper {
    private final StudentTransaction studentTransaction;

    @Autowired
    public StudentMapper(StudentTransaction studentTransaction) {
        this.studentTransaction = studentTransaction;
    }

    public StudentDTO toDTO(Student student) {
        StudentDTO studentDTO = new StudentDTO();

        studentDTO.lastName = student.getLastName();
        studentDTO.firstName = student.getFirstName();
        studentDTO.email = student.getEmail();
        studentDTO.studyCycle = student.getStudyCycle().toString();
        studentDTO.studyYear = String.valueOf(student.getStudyYear());
        studentDTO.group = String.valueOf(student.getGroup());

        return studentDTO;
    }

    public Student toEntity(StudentDTO studentDTO) {
        Student student = new Student();
        setEntity(student, studentDTO);
        return student;
    }

    public void setEntity(Student student, StudentDTO studentDTO) {
        studentTransaction.update(student, studentDTO);
    }
}
