package pos.alexandruchi.academia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.Student;

@Repository
public interface StudentRepository extends CrudRepository<Student, Integer> {
    public Student findByEmail(String email);
}
