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
        lectureDTO.lectureType = lecture.getLectureType().toString();
        lectureDTO.lectureCategory = lecture.getLectureCategory().toString();
        lectureDTO.examinationType = lecture.getExaminationType().toString();

        return lectureDTO;
    }

    public Lecture toEntity(LectureDTO lectureDTO, String code) {
        Lecture lecture = new Lecture();
        setEntity(lecture, lectureDTO);
        lecture.setId(code);
        return lecture;
    }

    public void setEntity(Lecture lecture, LectureDTO lectureDTO) {
        lectureTransaction.update(lecture, lectureDTO);
    }
}
