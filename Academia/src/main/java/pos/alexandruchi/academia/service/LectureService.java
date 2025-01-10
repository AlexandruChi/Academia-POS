package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.repository.LectureRepository;

import java.util.Optional;

@Service
public class LectureService {
    private final LectureRepository lectureRepository;

    @Autowired
    public LectureService(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    public Iterable<Lecture> getLectures() {
        return lectureRepository.findAll();
    }

    public Iterable<Lecture> getLectures(String type, String category) {
        if (type == null && category == null) {
            return getLectures();
        } else if (type == null) {
            return lectureRepository.findAllByLectureCategory(category);
        } else if (category == null) {
            return lectureRepository.findAllByLectureType(type);
        } else {
            return lectureRepository.findAllByLectureTypeAndLectureCategory(type, category);
        }
    }

    public Optional<Lecture> getLecture(String code) {
        return lectureRepository.findById(code);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Lecture setLecture(Lecture lecture) {
        if (lecture == null) {
            throw new IllegalArgumentException();
        }

        try {
            return lectureRepository.save(lecture);
        } catch (DataIntegrityViolationException e) {
            return null;
        }
    }

    public void deleteLecture(Lecture lecture) {
        try {
            lectureRepository.delete(lecture);
        } catch (Exception ignores) {}
    }
}
