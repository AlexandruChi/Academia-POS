package pos.alexandruchi.academia.mapper.Lecture;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pos.alexandruchi.academia.DTO.LectureDTO;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.service.ProfessorService;
import pos.alexandruchi.academia.converter.types.*;

@Component
public class LectureTransaction {
    private final ProfessorService professorService;

    @Autowired
    public LectureTransaction(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @Transactional
    void update(Lecture lecture, LectureDTO lectureDTO) {
        try {
            lecture.setIdHolder(
                    professorService.getProfessor(Integer.valueOf(lectureDTO.idHolder))
                            .orElseThrow(IllegalArgumentException::new)
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        lecture.setLectureName(lectureDTO.lectureName);
        lecture.setStudyYear(lectureDTO.studyYear);
        lecture.setLectureType(LectureType.of(lectureDTO.lectureType));
        lecture.setLectureCategory(LectureCategory.of(lectureDTO.lectureCategory));
        lecture.setExaminationType(ExaminationType.of(lectureDTO.examinationType));
    }
}