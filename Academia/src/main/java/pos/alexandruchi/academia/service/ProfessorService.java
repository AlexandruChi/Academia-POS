package pos.alexandruchi.academia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Professor;
import pos.alexandruchi.academia.repository.LectureRepository;
import pos.alexandruchi.academia.repository.ProfessorRepository;
import pos.alexandruchi.academia.types.TeachingDegree;

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

    public Page<Professor> getProfessors() {
        return getProfessors(Pageable.unpaged());
    }

    public Page<Professor> getProfessors(Pageable pageable) {
        return getProfessors(pageable, null, null);
    }

    public Page<Professor> getProfessors(String lastNameStart, TeachingDegree rank) {
        return getProfessors(Pageable.unpaged(), lastNameStart, rank);
    }

    public Page<Professor> getProfessors(Pageable pageable, String lastNameStart, TeachingDegree rank) {
        if (lastNameStart == null && rank == null) {
            return professorRepository.findAll(pageable);
        } else if (lastNameStart == null) {
            return professorRepository.findAllByTeachingDegree(rank, pageable);
        } else if (rank == null) {
            return professorRepository.findAllByLastNameStartsWith(lastNameStart, pageable);
        } else {
            return professorRepository.findAllByTeachingDegreeAndLastNameStartsWith(rank, lastNameStart, pageable);
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
