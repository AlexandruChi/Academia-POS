package pos.alexandruchi.academia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.LectureDTO;
import pos.alexandruchi.academia.mapper.Lecture.LectureMapper;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.service.LectureService;

@RestController
@RequestMapping("/lecture")
public class LectureController {
    private final LectureService lectureService;
    private final LectureMapper lectureMapper;

    @Autowired
    public LectureController(LectureService lectureService, LectureMapper lectureMapper) {
        this.lectureService = lectureService;
        this.lectureMapper = lectureMapper;
    }

    @GetMapping("/{code}")
    public LectureDTO getLecture(@PathVariable String code) {
        Lecture lecture = lectureService.getLecture(code);

        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return lectureMapper.toDTO(lecture);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{code}")
    public void setLecture(@PathVariable String code, @RequestBody LectureDTO lectureDTO) {
        Lecture lecture = lectureService.getLecture(code);

        if (lecture == null) {
            lecture = lectureService.addLecture(lectureMapper.toEntity(lectureDTO, code));
        } else {
            if (!lectureMapper.setEntity(lecture, lectureDTO)) {
                lecture = null;
            }
        }

        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{code}")
    public void deleteLecture(@PathVariable String code) {
        lectureService.deleteLecture(lectureService.getLecture(code));
    }
}
