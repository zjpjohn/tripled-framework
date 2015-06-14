package eu.tripled.eventbus.synchronous;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class EventHandlerInvoker {

  private Class<?> eventType;
  private Object eventHandler;
  private Method method;

  public EventHandlerInvoker(Class eventType, Object eventHandler, Method eventHandlerMethod) {
    this.eventType = eventType;
    this.eventHandler = eventHandler;
    this.method = eventHandlerMethod;
  }

  public boolean handles(Class<?> eventTypeToHandle) {
    return this.eventType.isAssignableFrom(eventTypeToHandle);
  }

  public boolean hasReturnType() {
    return !method.getReturnType().getName().equals("void");
  }


  public Object invoke(Object object) {
    try {
      return method.invoke(eventHandler, object);
    } catch (InvocationTargetException | IllegalAccessException e) {
      String errorMsg = String.format("Could not invoke EventHandler method %s on %s", method.getName(), eventHandler.getClass().getSimpleName());
      throw new EventHandlerInvocationException(errorMsg, e);
    }
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
    final EventHandlerInvoker other = (EventHandlerInvoker) obj;
    return Objects.equals(this.eventType, other.eventType)
        && Objects.equals(this.eventHandler, other.eventHandler)
        && Objects.equals(this.method, other.method);
  }
}
