package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Professor;
import pos.alexandruchi.academia.repository.LectureRepository;
import pos.alexandruchi.academia.repository.ProfessorRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepository;
    private final LectureRepository lectureRepository;

    @Autowired
    public ProfessorService(ProfessorRepository professorRepository, LectureRepository lectureRepository) {
        this.professorRepository = professorRepository;
        this.lectureRepository = lectureRepository;
    }

    public Iterable<Professor> getProfessors() {
        return professorRepository.findAll();
    }

    public Iterable<Professor> getProfessors(String lastNameStart, String rank) {
        if (lastNameStart == null && rank == null) {
            return getProfessors();
        } else if (lastNameStart == null) {
            return professorRepository.findAllByTeachingDegree(rank);
        } else if (rank == null) {
            return professorRepository.findAllByLastNameStartsWith(lastNameStart);
        } else {
            return professorRepository.findAllByTeachingDegreeAndLastNameStartsWith(rank, lastNameStart);
        }
    }

    public Optional<Professor> getProfessor(Integer id) {
        return professorRepository.findById(id);
    }

    public Professor setProfessor(Professor professor) {
        if (professor == null) {
            throw new IllegalArgumentException();
        }

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

    public Iterable<Lecture> getLectures(Professor professor) {
        return StreamSupport.stream(lectureRepository.findAll().spliterator(), false)
                .filter(lecture -> lecture.getIdHolder() == professor)
                .toList();
    }
}
