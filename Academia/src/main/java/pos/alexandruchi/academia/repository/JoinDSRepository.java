package pos.alexandruchi.academia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pos.alexandruchi.academia.model.JoinDS;
import pos.alexandruchi.academia.model.JoinDSId;

@Repository
public interface JoinDSRepository extends CrudRepository<JoinDS, JoinDSId> {
}
