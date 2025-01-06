package pos.alexandruchi.academia.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pos.alexandruchi.academia.DTO.LectureDTO;
import pos.alexandruchi.academia.exception.authorization.Unauthenticated;
import pos.alexandruchi.academia.exception.authorization.Unauthorized;
import pos.alexandruchi.academia.mapper.Lecture.LectureMapper;
import pos.alexandruchi.academia.model.Lecture;
import pos.alexandruchi.academia.model.Student;
import pos.alexandruchi.academia.service.*;
import pos.alexandruchi.academia.service.AuthorizationService.Role;
import pos.alexandruchi.academia.service.AuthorizationService.Claims;

import java.util.*;

@RestController
@RequestMapping("/lectures")
public class LectureController {
    private final LectureService lectureService;
    private final LectureMapper lectureMapper;
    private final EnrollService enrollService;
    private final AuthorizationService authorizationService;

    @Autowired
    public LectureController(LectureService lectureService, LectureMapper lectureMapper, EnrollService enrollService, AuthorizationService authorizationService) {
        this.lectureService = lectureService;
        this.lectureMapper = lectureMapper;
        this.enrollService = enrollService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Map<String, Object>> getLectures(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(
                Role.ADMIN, Role.PROFESSOR, Role.STUDENT
        ));

        List<Map<String, Object>> lectures = new ArrayList<>();

        for (Lecture lecture : lectureService.getLectures()) {
            if (!CheckAcces(claims, lecture)) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("code", lecture.getCode());
            map.put("lecture", lectureMapper.toDTO(lecture));
            lectures.add(map);
        }

        return lectures;
    }

    @GetMapping("/{code}")
    public LectureDTO getLecture(
            @PathVariable String code, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Claims claims = CheckAuthorization(authorization, List.of(
                Role.ADMIN, Role.PROFESSOR, Role.STUDENT
        ));

        Lecture lecture = lectureService.getLecture(code).orElse(null);

        if (!CheckAcces(claims, lecture)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return lectureMapper.toDTO(lecture);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{code}", consumes = "application/json")
    public void setLecture(
            @PathVariable String code, @RequestBody(required = false) LectureDTO lectureDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN ));
        Lecture lecture = lectureService.getLecture(code).orElse(null);

        try {
            if (lecture == null) {
                lecture = lectureMapper.toEntity(lectureDTO, code);
            } else {
                lectureMapper.setEntity(lecture, lectureDTO);
            }

            if (lecture == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }

            lectureService.setLecture(lecture);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{code}")
    public void deleteLecture(
            @PathVariable String code, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        CheckAuthorization(authorization, List.of(Role.ADMIN));
        lectureService.deleteLecture(lectureService.getLecture(code).orElse(null));
    }

    /// Check if user has the required role and sends response appropriate code otherwise
    private @NotNull Claims CheckAuthorization(String authorization, List<Role> roles) {
        try {
            return authorizationService.checkAuthorization(authorization, roles);
        } catch (Unauthenticated e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    /// Check if a user has access to a lecture
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean CheckAcces(@NotNull Claims claims, Lecture lecture) {
        return claims.role() != AuthorizationService.Role.STUDENT || enrollService.isEnrolled(
                (Student) authorizationService.getEntity(claims), lecture);
    }
}
