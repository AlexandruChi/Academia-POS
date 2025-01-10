package pos.alexandruchi.academia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.Professor;

@Repository
public interface ProfessorRepository extends CrudRepository<Professor, Integer> {
    Professor findByEmail(String email);

    Iterable<Professor> findAllByTeachingDegree(String teachingDegree);
    Iterable<Professor> findAllByLastNameStartsWith(String lastName);

    Iterable<Professor> findAllByTeachingDegreeAndLastNameStartsWith(String teachingDegree, String lastName);
}
