public class NoMatchingActivitiesFoundException extends Throwable {
    private String message;

    public NoMatchingActivitiesFoundException(String activityNotFound){
        this.message = "Could not find " + activityNotFound;
    }

    public String getMessage(){
        return message;
    }
}
