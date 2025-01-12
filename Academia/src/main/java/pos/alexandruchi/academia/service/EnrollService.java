package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.JoinDSId;
import pos.alexandruchi.academia.model.JoinDS;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.repository.JoinDSRepository;

@Service
public class EnrollService {
    private final JoinDSRepository joinDSRepository;

    @Autowired
    public EnrollService(JoinDSRepository joinDSRepository) {
        this.joinDSRepository = joinDSRepository;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEnrolled(Student student, Lecture lecture) {
        if (student == null || lecture == null) {
            return false;
        }

        JoinDSId joinDSId = new JoinDSId();
        joinDSId.setStudentID(student.getId());
        joinDSId.setLectureID(lecture.getId());
        return joinDSRepository.existsById(joinDSId);
    }

    public boolean enroll(Student student, Lecture lecture) {
        if (student == null || lecture == null) {
            throw new IllegalArgumentException();
        }

        JoinDS joinDS = new JoinDS();
        joinDS.setId(new JoinDSId());
        joinDS.setStudentID(student);
        joinDS.setLectureID(lecture);

        try {
            joinDSRepository.save(joinDS);
        } catch (DataIntegrityViolationException e) {
            return false;
        }

        return true;
    }

    public void unenroll(Student student, Lecture lecture) {
        if (student == null || lecture == null) {
            return;
        }

        joinDSRepository.findByStudentIDAndLectureID(student, lecture).ifPresent(joinDSRepository::delete);
    }
}
