package pos.alexandruchi.academia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "lectures")
public class Lecture {
    @Id
    @JsonIgnore
    @Column(name = "CODE", nullable = false, length = 100)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_holder", nullable = false)
    private pos.alexandruchi.academia.model.Professor idHolder;

    @Column(name = "lecture_name", nullable = false, length = 100)
    private String lectureName;

    @Column(name = "study_year", nullable = false, length = 100)
    private String studyYear;

    @Lob
    @Column(name = "lecture_type", nullable = false)
    private String lectureType;

    @Lob
    @Column(name = "lecture_category", nullable = false)
    private String lectureCategory;

    @Lob
    @Column(name = "examination_type", nullable = false)
    private String examinationType;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public pos.alexandruchi.academia.model.Professor getIdHolder() {
        return idHolder;
    }

    public void setIdHolder(pos.alexandruchi.academia.model.Professor idHolder) {
        this.idHolder = idHolder;
    }

    public String getLectureName() {
        return lectureName;
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }

    public String getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(String studyYear) {
        this.studyYear = studyYear;
    }

    public String getLectureType() {
        return lectureType;
    }

    public void setLectureType(String lectureType) {
        this.lectureType = lectureType;
    }

    public String getLectureCategory() {
        return lectureCategory;
    }

    public void setLectureCategory(String lectureCategory) {
        this.lectureCategory = lectureCategory;
    }

    public String getExaminationType() {
        return examinationType;
    }

    public void setExaminationType(String examinationType) {
        this.examinationType = examinationType;
    }

}