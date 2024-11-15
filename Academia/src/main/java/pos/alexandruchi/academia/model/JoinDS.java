package pos.alexandruchi.academia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "join_ds")
public class JoinDS {
    @EmbeddedId
    private JoinDSId id;

    @MapsId("disciplineID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DisciplineID", nullable = false)
    private Lecture lectureID;

    @MapsId("studentID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "StudentID", nullable = false)
    private pos.alexandruchi.academia.model.Student studentID;

    public JoinDSId getId() {
        return id;
    }

    public void setId(JoinDSId id) {
        this.id = id;
    }

    public Lecture getLectureID() {
        return lectureID;
    }

    public void setLectureID(Lecture lectureID) {
        this.lectureID = lectureID;
    }

    public pos.alexandruchi.academia.model.Student getStudentID() {
        return studentID;
    }

    public void setStudentID(pos.alexandruchi.academia.model.Student studentID) {
        this.studentID = studentID;
    }

}