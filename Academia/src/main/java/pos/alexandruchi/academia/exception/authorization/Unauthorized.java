package pos.alexandruchi.academia.exception.authorization;

public class Unauthorized extends RuntimeException {
    public Unauthorized() {
        super();
    }

    public Unauthorized(String message) {
        super(message);
    }
}
