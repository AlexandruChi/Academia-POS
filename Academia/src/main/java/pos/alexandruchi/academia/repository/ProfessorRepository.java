package pos.alexandruchi.academia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.Professor;

import pos.alexandruchi.academia.types.TeachingDegree;

import java.util.Optional;

@Repository
public interface ProfessorRepository
        extends PagingAndSortingRepository<Professor, Integer>, CrudRepository<Professor, Integer> {
    Optional<Professor> findByEmail(String email);

    Page<Professor> findAllByTeachingDegree(TeachingDegree teachingDegree, Pageable pageable);
    Page<Professor> findAllByLastNameStartsWith(String lastName, Pageable pageable);
    Page<Professor> findAllByTeachingDegreeAndLastNameStartsWith(
            TeachingDegree teachingDegree, String lastName, Pageable pageable
    );
}
