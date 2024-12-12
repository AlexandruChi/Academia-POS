package pos.alexandruchi.academia.mapper.Professor;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import pos.alexandruchi.academia.DTO.ProfessorDTO;
import pos.alexandruchi.academia.model.Professor;

@Component
public class ProfessorTransaction {
    @Transactional
    void update(Professor professor, ProfessorDTO professorDTO) {
        professor.setLastName(professorDTO.lastName);
        professor.setFirstName(professorDTO.firstName);
        professor.setEmail(professorDTO.email);
        professor.setTeachingDegree(professorDTO.teachingDegree);
        professor.setAssociationType(professorDTO.associationType);
        professor.setAffiliation(professorDTO.affiliation);
    }
}
