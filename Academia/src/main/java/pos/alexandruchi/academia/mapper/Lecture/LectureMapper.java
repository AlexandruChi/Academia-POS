package pos.alexandruchi.academia.mapper.Lecture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pos.alexandruchi.academia.DTO.LectureDTO;
import pos.alexandruchi.academia.model.Lecture;

@Component
public class LectureMapper {
    private final LectureTransaction lectureTransaction;

    @Autowired
    public LectureMapper(LectureTransaction lectureTransaction) {
        this.lectureTransaction = lectureTransaction;
    }

    public LectureDTO toDTO(Lecture lecture) {
        LectureDTO lectureDTO = new LectureDTO();

        lectureDTO.idHolder = String.valueOf(lecture.getIdHolder().getId());
        lectureDTO.lectureName = lecture.getLectureName();
        lectureDTO.studyYear = lecture.getStudyYear();
        lectureDTO.lectureType = lecture.getLectureType();
        lectureDTO.lectureCategory = lecture.getLectureCategory();
        lectureDTO.examinationType = lecture.getExaminationType();

        return lectureDTO;
    }

    public Lecture toEntity(LectureDTO lectureDTO, String code) {
        Lecture lecture = new Lecture();

        if (setEntity(lecture, lectureDTO)) {
            lecture.setCode(code);
            return lecture;
        }

        return null;
    }

    public boolean setEntity(Lecture lecture, LectureDTO lectureDTO) {
        try {
            lectureTransaction.update(lecture, lectureDTO);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
