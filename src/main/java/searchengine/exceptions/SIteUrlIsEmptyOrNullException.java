package searchengine.exceptions;

public class SIteUrlIsEmptyOrNullException extends RuntimeException {
    public SIteUrlIsEmptyOrNullException(String siteName) {
        super("Site <" + siteName + "> has url empty or null!");
    }
}