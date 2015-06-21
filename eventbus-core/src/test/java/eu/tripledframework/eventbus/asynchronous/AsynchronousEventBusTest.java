package eu.tripledframework.eventbus.asynchronous;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import eu.tripledframework.eventbus.*;
import eu.tripledframework.eventbus.callback.FutureEventCallback;
import eu.tripledframework.eventbus.interceptor.TestValidator;
import eu.tripledframework.eventbus.interceptor.ValidatingEventBusInterceptor;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class AsynchronousEventBusTest {

  public static final String THREAD_POOL_PREFIX = "CDForTest-pool-";
  public static final String THREAD_POOL_WITH_VALIDATION_PREFIX = "CDForTest-Val-pool-";
  private EventPublisher asynchronousDispatcher;
  private EventPublisher dispatcherWithValidation;
  private TestEventHandler eventHandler;
  private TestValidator validator;

  @Before
  public void setUp() throws Exception {
    eventHandler = new TestEventHandler();

    ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
        .setNameFormat(THREAD_POOL_PREFIX + "%d")
        .build());
    AsynchronousEventBus eventBus = new AsynchronousEventBus(executor);
    eventBus.subscribe(eventHandler);
    asynchronousDispatcher = eventBus;

    ExecutorService executor2 = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
        .setNameFormat(THREAD_POOL_WITH_VALIDATION_PREFIX + "%d")
        .build());
    validator = new TestValidator();
    List<EventBusInterceptor> interceptors = Lists.newArrayList(new ValidatingEventBusInterceptor(validator));
    AsynchronousEventBus eventBus2 = new AsynchronousEventBus(interceptors, executor2);
    eventBus2.subscribe(eventHandler);
    dispatcherWithValidation = eventBus2;

  }

  @Test
  public void whenNotGivenAnExecutor_shouldCreateADefaultOneAndExecuteCommands() throws Exception {
    // given
    AsynchronousEventBus defaultPublisher = new AsynchronousEventBus();
    TestEventHandler eventHandler = new TestEventHandler();
    defaultPublisher.subscribe(eventHandler);
    Future<Void> future = FutureEventCallback.forType(Void.class);
    HelloCommand command = new HelloCommand("domenique");

    // when
    defaultPublisher.publish(command, future);
    future.get();

    // then
    assertThat(future.isDone(), is(true));
    assertThat(eventHandler.isHelloCommandHandled, is(true));
  }

  @Test
  public void whenGivenAValidCommand_shouldBeExecutedAsynchronous() throws Exception {
    // given
    HelloCommand command = new HelloCommand("Domenique");
    Future<Void> future = FutureEventCallback.forType(Void.class);

    // when
    asynchronousDispatcher.publish(command, future);
    future.get();

    // then
    assertThat(eventHandler.isHelloCommandHandled, is(true));
    assertThat(eventHandler.threadNameForExecute, equalTo(THREAD_POOL_PREFIX + "0"));
  }

  @Test
  public void whenGivenAValidCommandAndFutureCallback_waitForExecutionToFinish() throws Exception {
    // given
    HelloCommand command = new HelloCommand("Domenique");
    Future<Void> future = FutureEventCallback.forType(Void.class);

    // when
    asynchronousDispatcher.publish(command, future);
    future.get();

    // then
    assertThat(future.isDone(), is(true));
    assertThat(eventHandler.isHelloCommandHandled, is(true));
    assertThat(eventHandler.threadNameForExecute, equalTo(THREAD_POOL_PREFIX + "0"));
  }

  @Test
  public void whenUsingADispatcherWithValidator_validationShouldBeDoneAsynchronous() throws Exception {
    // given
    Future<Void> future = FutureEventCallback.forType(Void.class);
    ValidatingCommand command = new ValidatingCommand("should pass");
    validator.shouldFailNextCall(false);

    // when
    dispatcherWithValidation.publish(command, future);
    future.get();

    // then
    assertThat(future.isDone(), is(true));
    assertThat(eventHandler.isValidatingCommandHandled, is(true));
    assertThat(eventHandler.threadNameForExecute, equalTo(THREAD_POOL_WITH_VALIDATION_PREFIX + "0"));
  }
}