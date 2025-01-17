package pos.alexandruchi.academia.service;

import org.springframework.stereotype.Service;
import pos.alexandruchi.academia.exception.authorization.Unauthenticated;
import pos.alexandruchi.academia.exception.authorization.Unauthorized;
import pos.alexandruchi.academia.repository.ProfessorRepository;
import pos.alexandruchi.academia.repository.StudentRepository;

import java.util.List;

@Service
public class AuthorizationService {

    public enum Role {ADMIN, SERVICE, STUDENT, PROFESSOR}

    public record Claims(String email, Role role) {}

    private final IDMService idmService;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;

    public AuthorizationService(IDMService idmService, StudentRepository studentRepository, ProfessorRepository professorRepository) {
        this.idmService = idmService;
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
    }

    /// Check if a user has the required roles
    public Claims checkAuthorization(String authorization, List<Role> roles) {
        Claims claims = idmService.getClaims(authorization);
        if (claims == null) {
            throw new Unauthenticated();
        }

        if (!roles.contains(claims.role())) {
            throw new Unauthorized();
        }

        return claims;
    }

    /// Return database entity for user
    public Object getEntity(Claims claims) {
        return switch (claims.role()) {
            case STUDENT -> studentRepository.findByEmail(claims.email());
            case PROFESSOR -> professorRepository.findByEmail(claims.email());
            default -> null;
        };
    }
}
