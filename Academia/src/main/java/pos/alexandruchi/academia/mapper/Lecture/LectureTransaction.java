package pos.alexandruchi.academia.mapper.Lecture;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pos.alexandruchi.academia.DTO.LectureDTO;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.service.ProfessorService;

@Component
public class LectureTransaction {
    private final ProfessorService professorService;

    @Autowired
    public LectureTransaction(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @Transactional
    void update(Lecture lecture, LectureDTO lectureDTO) {
        lecture.setIdHolder(professorService.getProfessor(Integer.valueOf(lectureDTO.idHolder)));
        lecture.setLectureName(lectureDTO.lectureName);
        lecture.setStudyYear(lectureDTO.studyYear);
        lecture.setLectureType(lectureDTO.lectureType);
        lecture.setLectureCategory(lectureDTO.lectureCategory);
        lecture.setExaminationType(lectureDTO.examinationType);
    }
}