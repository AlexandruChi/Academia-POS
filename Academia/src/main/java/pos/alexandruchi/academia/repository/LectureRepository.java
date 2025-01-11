package pos.alexandruchi.academia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.converter.types.*;

@Repository
public interface LectureRepository
        extends PagingAndSortingRepository<Lecture, String>, CrudRepository<Lecture, String> {
    Page<Lecture> findAllByLectureType(LectureType lectureType, Pageable pageable);
    Page<Lecture> findAllByLectureCategory(LectureCategory lectureCategory, Pageable pageable);
    Page<Lecture> findAllByLectureTypeAndLectureCategory(LectureType lectureType, LectureCategory lectureCategory, Pageable pageable);
}
