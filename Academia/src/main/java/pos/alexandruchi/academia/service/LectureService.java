package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.repository.LectureRepository;

@Service
public class LectureService {
    private final LectureRepository lectureRepository;

    @Autowired
    public LectureService(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    public Lecture getLecture(String code) {
        return lectureRepository.findById(code).orElse(null);
    }

    public Lecture setLecture(Lecture lecture) {
        try {
            return lectureRepository.save(lecture);
        } catch (DataIntegrityViolationException e) {
            return null;
        }
    }

    public void deleteLecture(Lecture lecture) {
        lectureRepository.delete(lecture);
    }
}
