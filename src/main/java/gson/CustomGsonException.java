package gson;

public class CustomGsonException extends Exception {

    public CustomGsonException(final String customMessage) {
        super(customMessage);
    }
    public CustomGsonException(final Exception e){
        super(e);
    }
}
