package pos.alexandruchi.academia.model;

import jakarta.persistence.*;
import pos.alexandruchi.academia.converter.types.*;

@Entity
@Table(name = "students")
@Access(AccessType.PROPERTY)
public class Student {
    private Integer id;
    private String lastName;
    private String firstName;
    private String email;
    private StudyCycle studyCycle;
    private Integer studyYear;
    private Integer group;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "last_name", nullable = false, length = 100)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "first_name", nullable = false, length = 100)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "email", nullable = false, length = 100)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "study_cycle", nullable = false, length = 100)
    public StudyCycle getStudyCycle() {
        return studyCycle;
    }

    public void setStudyCycle(StudyCycle studyCycle) {
        this.studyCycle = studyCycle;
    }

    @Column(name = "study_year", nullable = false)
    public Integer getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(Integer studyYear) {
        this.studyYear = studyYear;
    }

    @Column(name = "`group`", nullable = false)
    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}