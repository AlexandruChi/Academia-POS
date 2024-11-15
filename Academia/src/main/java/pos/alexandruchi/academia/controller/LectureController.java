package pos.alexandruchi.academia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.LectureDTO;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.service.LectureService;
import pos.alexandruchi.academia.service.ProfessorService;

@RestController
@RequestMapping("/lecture")
public class LectureController {
    private final LectureService lectureService;
    private final ProfessorService professorService;

    @Autowired
    public LectureController(LectureService lectureService, ProfessorService professorService) {
        this.lectureService = lectureService;
        this.professorService = professorService;
    }

    @GetMapping("/{code}")
    public Lecture getLecture(@PathVariable String code) {
        Lecture lecture = lectureService.getLecture(code);

        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return lecture;
    }

    @PutMapping("/{code}")
    public void Lecture(@PathVariable String code, @RequestBody LectureDTO lectureDTO) {
        Lecture lecture = lectureService.getLecture(code);

        if (lecture == null) {
            lecture = new Lecture();
        }

        lecture.setCode(code);
        lecture.setIdHolder(professorService.getProfessor(Integer.valueOf(lectureDTO.idHolder)));
        lecture.setLectureName(lectureDTO.lectureName);
        lecture.setStudyYear(lectureDTO.studyYear);
        lecture.setLectureType(lectureDTO.lectureType);
        lecture.setLectureCategory(lectureDTO.lectureCategory);
        lecture.setExaminationType(lectureDTO.examinationType);

        if (lectureService.setLecture(lecture) == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }
}
