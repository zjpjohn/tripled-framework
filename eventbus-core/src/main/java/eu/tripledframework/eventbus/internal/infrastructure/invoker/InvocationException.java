package eu.tripledframework.eventbus.internal.infrastructure.invoker;

public class InvocationException extends RuntimeException {

  public InvocationException(String message, Throwable cause) {
    super(message, cause);
  }
}
