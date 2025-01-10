package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
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
        joinDSId.setDisciplineID(lecture.getCode());
        return joinDSRepository.existsById(joinDSId);
    }

    public void enroll(Student student, Lecture lecture) {
        if (student == null || lecture == null) {
            return;
        }

        JoinDS joinDS = new JoinDS();
        joinDS.setStudentID(student);
        joinDS.setLectureID(lecture);
        joinDSRepository.save(joinDS);
    }

    public void unenroll(Student student, Lecture lecture) {
        if (student == null || lecture == null) {
            return;
        }

        joinDSRepository.findByStudentIDAndLectureID(student, lecture).ifPresent(joinDSRepository::delete);
    }
}
