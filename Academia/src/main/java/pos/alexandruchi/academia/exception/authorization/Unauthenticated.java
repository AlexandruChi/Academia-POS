package pos.alexandruchi.academia.exception.authorization;

public class Unauthenticated extends RuntimeException {
    public Unauthenticated() {
        super();
    }

    public Unauthenticated(String message) {
        super(message);
    }
}
