package ua.notion.infrastructure.async;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncResult<T> {
  private final T value;
  private final Throwable error;
  private final boolean success;

  private AsyncResult(T value, Throwable error, boolean success) {
    this.value = value;
    this.error = error;
    this.success = success;
  }

  public static <T> AsyncResult<T> success(T value) {
    return new AsyncResult<>(value, null, true);
  }

  public static <T> AsyncResult<T> failure(Throwable error) {
    return new AsyncResult<>(null, error, false);
  }

  public boolean isSuccess() {
    return success;
  }

  public boolean isFailure() {
    return !success;
  }

  public Optional<T> getValue() {
    return Optional.ofNullable(value);
  }

  public Optional<Throwable> getError() {
    return Optional.ofNullable(error);
  }

  public T getOrThrow() {
    if (success) {
      return value;
    }
    throw new RuntimeException("AsyncResult failed", error);
  }

  public T getOrDefault(T defaultValue) {
    return success ? value : defaultValue;
  }

  public <U> AsyncResult<U> map(Function<T, U> mapper) {
    if (success) {
      try {
        return AsyncResult.success(mapper.apply(value));
      } catch (Exception e) {
        return AsyncResult.failure(e);
      }
    }
    return AsyncResult.failure(error);
  }

  public void ifSuccess(Consumer<T> consumer) {
    if (success) {
      consumer.accept(value);
    }
  }

  public void ifFailure(Consumer<Throwable> consumer) {
    if (!success) {
      consumer.accept(error);
    }
  }
}
