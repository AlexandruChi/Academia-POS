package pos.alexandruchi.academia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.Student;

import java.util.Optional;

@Repository
public interface StudentRepository
        extends PagingAndSortingRepository<Student, Integer>, CrudRepository<Student, Integer> {
    Optional<Student> findByEmail(String email);
}
