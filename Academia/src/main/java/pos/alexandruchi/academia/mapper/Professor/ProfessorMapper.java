package pos.alexandruchi.academia.mapper.Professor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pos.alexandruchi.academia.DTO.ProfessorDTO;
import pos.alexandruchi.academia.model.Professor;

@Component
public class ProfessorMapper {
    private final ProfessorTransaction professorTransaction;

    @Autowired
    public ProfessorMapper(ProfessorTransaction professorTransaction) {
        this.professorTransaction = professorTransaction;
    }

    public ProfessorDTO toDTO(Professor professor) {
        ProfessorDTO professorDTO = new ProfessorDTO();

        professorDTO.lastName = professor.getLastName();
        professorDTO.firstName = professor.getFirstName();
        professorDTO.email = professor.getEmail();
        professorDTO.teachingDegree = professor.getTeachingDegree().toString();
        professorDTO.associationType = professor.getAssociationType().toString();
        professorDTO.affiliation = professor.getAffiliation();

        return professorDTO;
    }

    public Professor toEntity(ProfessorDTO professorDTO) {
        Professor professor = new Professor();
        setEntity(professor, professorDTO);
        return professor;
    }

    public void setEntity(Professor professor, ProfessorDTO professorDTO) {
        professorTransaction.update(professor, professorDTO);
    }
}
