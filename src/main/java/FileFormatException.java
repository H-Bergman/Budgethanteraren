public class FileFormatException extends RuntimeException {

    public FileFormatException(String message) {
        super(message);
    }

    public FileFormatException() {
        super("Something went wrong when formatting!");
    }
}
