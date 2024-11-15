package pos.alexandruchi.academia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.Professor;

@Repository
public interface ProfessorRepository extends CrudRepository<Professor, Integer> {
}
