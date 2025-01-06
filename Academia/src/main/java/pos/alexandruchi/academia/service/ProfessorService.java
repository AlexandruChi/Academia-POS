package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.Professor;
import pos.alexandruchi.academia.repository.ProfessorRepository;

import java.util.Optional;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepository;

    @Autowired
    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public Iterable<Professor> getProfessors() {
        return professorRepository.findAll();
    }

    public Optional<Professor> getProfessor(Integer id) {
        return professorRepository.findById(id);
    }

    public Professor setProfessor(Professor professor) {
        try {
            return professorRepository.save(professor);
        } catch (DataIntegrityViolationException e) {
            return null;
        }
    }

    public void deleteProfessor(Professor professor) {
        try {
            professorRepository.delete(professor);
        } catch (Exception ignores) {}
    }
}
