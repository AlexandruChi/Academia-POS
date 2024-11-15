package pos.alexandruchi.academia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.util.Objects;

@Embeddable
public class JoinDSId implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = -490483343631178249L;
    @Column(name = "DisciplineID", nullable = false, length = 100)
    private String disciplineID;

    @Column(name = "StudentID", nullable = false)
    private Integer studentID;

    public String getDisciplineID() {
        return disciplineID;
    }

    public void setDisciplineID(String disciplineID) {
        this.disciplineID = disciplineID;
    }

    public Integer getStudentID() {
        return studentID;
    }

    public void setStudentID(Integer studentID) {
        this.studentID = studentID;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        JoinDSId entity = (JoinDSId) o;
        return Objects.equals(this.studentID, entity.studentID) &&
                Objects.equals(this.disciplineID, entity.disciplineID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentID, disciplineID);
    }

}