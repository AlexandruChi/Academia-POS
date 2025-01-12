package pos.alexandruchi.academia.model;

import jakarta.persistence.*;

import pos.alexandruchi.academia.types.ExaminationType;
import pos.alexandruchi.academia.types.LectureCategory;
import pos.alexandruchi.academia.types.LectureType;

@Entity
@Table(name = "lectures")
@Access(AccessType.PROPERTY)
public class Lecture {
    private String id;
    private pos.alexandruchi.academia.model.Professor idHolder;
    private String lectureName;
    private String studyYear;
    private LectureType lectureType;
    private LectureCategory lectureCategory;
    private ExaminationType examinationType;

    @Id
    @Column(name = "CODE", nullable = false, length = 100)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_holder", nullable = false)
    public pos.alexandruchi.academia.model.Professor getIdHolder() {
        return idHolder;
    }

    public void setIdHolder(pos.alexandruchi.academia.model.Professor idHolder) {
        this.idHolder = idHolder;
    }

    @Column(name = "lecture_name", nullable = false, length = 100)
    public String getLectureName() {
        return lectureName;
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }

    @Column(name = "study_year", nullable = false, length = 100)
    public String getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(String studyYear) {
        this.studyYear = studyYear;
    }

    @Column(name = "lecture_type", nullable = false)
    public LectureType getLectureType() {
        return lectureType;
    }

    public void setLectureType(LectureType lectureType) {
        this.lectureType = lectureType;
    }

    @Column(name = "lecture_category", nullable = false)
    public LectureCategory getLectureCategory() {
        return lectureCategory;
    }

    public void setLectureCategory(LectureCategory lectureCategory) {
        this.lectureCategory = lectureCategory;
    }

    @Column(name = "examination_type", nullable = false)
    public ExaminationType getExaminationType() {
        return examinationType;
    }

    public void setExaminationType(ExaminationType examinationType) {
        this.examinationType = examinationType;
    }
}