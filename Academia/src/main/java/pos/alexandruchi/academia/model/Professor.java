package pos.alexandruchi.academia.model;

import jakarta.persistence.*;

import pos.alexandruchi.academia.types.AssociationType;
import pos.alexandruchi.academia.types.TeachingDegree;

@Entity
@Table(name = "professors")
@Access(AccessType.PROPERTY)
public class Professor {
    private Integer id;
    private String lastName;
    private String firstName;
    private String email;
    private TeachingDegree teachingDegree;
    private AssociationType associationType;
    private String affiliation;

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

    @Column(name = "`first name`", nullable = false, length = 100)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "email", length = 100)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "teaching_degree")
    public TeachingDegree getTeachingDegree() {
        return teachingDegree;
    }

    public void setTeachingDegree(TeachingDegree teachingDegree) {
        this.teachingDegree = teachingDegree;
    }

    @Column(name = "association_type")
    public AssociationType getAssociationType() {
        return associationType;
    }

    public void setAssociationType(AssociationType associationType) {
        this.associationType = associationType;
    }

    @Column(name = "affiliation", length = 100)
    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
}