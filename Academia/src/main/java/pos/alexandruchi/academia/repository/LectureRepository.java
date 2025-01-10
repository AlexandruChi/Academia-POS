package pos.alexandruchi.academia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.Lecture;

@Repository
public interface LectureRepository extends CrudRepository<Lecture, String> {
    Iterable<Lecture> findAllByLectureType(String lectureType);
    Iterable<Lecture> findAllByLectureCategory(String lectureCategory);

    Iterable<Lecture> findAllByLectureTypeAndLectureCategory(String lectureType, String lectureCategory);
}
