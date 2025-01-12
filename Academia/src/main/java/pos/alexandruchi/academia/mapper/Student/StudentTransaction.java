package pos.alexandruchi.academia.mapper.Student;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import pos.alexandruchi.academia.DTO.StudentDTO;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.types.StudyCycle;

@Component
public class StudentTransaction {
    @Transactional
    void update(Student student, StudentDTO studentDTO) {
        student.setLastName(studentDTO.lastName);
        student.setFirstName(studentDTO.firstName);
        student.setEmail(studentDTO.email);
        student.setStudyCycle(StudyCycle.of(studentDTO.studyCycle));

        try {
            student.setStudyYear(Integer.valueOf(studentDTO.studyYear));
            student.setGroup(Integer.valueOf(studentDTO.group));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
