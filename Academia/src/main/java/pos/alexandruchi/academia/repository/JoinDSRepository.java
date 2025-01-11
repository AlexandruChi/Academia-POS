package pos.alexandruchi.academia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.JoinDS;
import pos.alexandruchi.academia.model.JoinDSId;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Student;

import java.util.Optional;

@Repository
public interface JoinDSRepository extends CrudRepository<JoinDS, JoinDSId> {
    Iterable<JoinDS> findAllByStudentID(Student studentID);
    Iterable<JoinDS> findAllByLectureID(Lecture lectureID);
    Optional<JoinDS> findByStudentIDAndLectureID(Student studentID, Lecture lectureID);
}
