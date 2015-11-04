package eu.tripledframework.eventbus.domain.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimpleEventHandlerInvoker implements EventHandlerInvoker {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEventHandlerInvoker.class);

  private final Class<?> eventType;
  private final Object eventHandler;
  private final Method method;

  public SimpleEventHandlerInvoker(Class eventType, Object eventHandler, Method eventHandlerMethod) {
    this.eventType = eventType;
    this.eventHandler = eventHandler;
    this.method = eventHandlerMethod;
  }

  @Override
  public boolean handles(Class<?> eventTypeToHandle) {
    return this.eventType.isAssignableFrom(eventTypeToHandle);
  }

  @Override
  public Class<?> getEventType() {
    return eventType;
  }

  @Override
  public boolean hasReturnType() {
    return !method.getReturnType().getName().equals("void");
  }

  @Override
  public Object invoke(Object object) {
    LOGGER.debug("About to invoke {}.{}() with event {}", eventHandler.getClass().getSimpleName(), method.getName(), object);
    try {
      return method.invoke(eventHandler, object);
    } catch (IllegalAccessException e) {
      String errorMsg = String.format("Could not invoke EventHandler method %s on %s", method.getName(), eventHandler.getClass().getSimpleName());
      throw new EventHandlerInvocationException(errorMsg, e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      } else {
        throw new EventHandlerInvocationException(
            "The invocation of the event handler threw an unknown checked exception.", e);
      }
    }
  }

  @Override
  public String toString() {
    return "EventHandlerInvoker{" +
           "eventType=" + eventType +
           ", eventHandler=" + eventHandler +
           ", method=" + method +
           '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventType, eventHandler, method);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SimpleEventHandlerInvoker other = (SimpleEventHandlerInvoker) obj;
    return Objects.equals(this.eventType, other.eventType)
        && Objects.equals(this.eventHandler, other.eventHandler)
        && Objects.equals(this.method, other.method);
  }
}