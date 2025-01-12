package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.JoinDS;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.repository.JoinDSRepository;
import pos.alexandruchi.academia.repository.LectureRepository;
import pos.alexandruchi.academia.types.LectureCategory;
import pos.alexandruchi.academia.types.LectureType;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class LectureService {
    private final LectureRepository lectureRepository;
    private final JoinDSRepository joinDSRepository;

    @Autowired
    public LectureService(LectureRepository lectureRepository, JoinDSRepository joinDSRepository) {
        this.lectureRepository = lectureRepository;
        this.joinDSRepository = joinDSRepository;
    }

    public Page<Lecture> getLectures() {
        return getLectures(Pageable.unpaged());
    }

    public Page<Lecture> getLectures(Pageable pageable) {
        return getLectures(pageable, null, null);
    }

    public Page<Lecture> getLectures(LectureType type, LectureCategory category) {
        return getLectures(Pageable.unpaged(), type, category);
    }

    public Page<Lecture> getLectures(Pageable pageable, LectureType type, LectureCategory category) {
        if (type == null && category == null) {
            return lectureRepository.findAll(pageable);
        } else if (type == null) {
            return lectureRepository.findAllByLectureCategory(category, pageable);
        } else if (category == null) {
            return lectureRepository.findAllByLectureType(type, pageable);
        } else {
            return lectureRepository.findAllByLectureTypeAndLectureCategory(type, category, pageable);
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

    public Iterable<Student> getStudents(Lecture lecture) {
        return StreamSupport.stream(joinDSRepository.findAllByLectureID(lecture).spliterator(), false)
                .map(JoinDS::getStudentID).toList();
    }
}
