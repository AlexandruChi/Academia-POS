package pos.alexandruchi.academia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "join_ds")
@Access(AccessType.PROPERTY)
public class JoinDS {
    private JoinDSId id;
    private Lecture lectureID;
    private pos.alexandruchi.academia.model.Student studentID;

    @EmbeddedId
    public JoinDSId getId() {
        return id;
    }

    public void setId(JoinDSId id) {
        this.id = id;
    }

    @MapsId("disciplineID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DisciplineID", nullable = false)
    public Lecture getLectureID() {
        return lectureID;
    }

    public void setLectureID(Lecture lectureID) {
        this.lectureID = lectureID;
    }

    @MapsId("studentID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "StudentID", nullable = false)
    public pos.alexandruchi.academia.model.Student getStudentID() {
        return studentID;
    }

    public void setStudentID(pos.alexandruchi.academia.model.Student studentID) {
        this.studentID = studentID;
    }

}