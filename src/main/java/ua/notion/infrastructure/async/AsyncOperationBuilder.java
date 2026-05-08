package ua.notion.infrastructure.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncOperationBuilder<T> {
  private final List<Supplier<CompletableFuture<?>>> operations = new ArrayList<>();
  private Supplier<T> finalOperation;
  private Consumer<T> onSuccess;
  private Consumer<Throwable> onError;

  public AsyncOperationBuilder<T> addStep(Supplier<CompletableFuture<?>> operation) {
    operations.add(operation);
    return this;
  }

  public AsyncOperationBuilder<T> then(Supplier<T> operation) {
    this.finalOperation = operation;
    return this;
  }

  public AsyncOperationBuilder<T> onSuccess(Consumer<T> callback) {
    this.onSuccess = callback;
    return this;
  }

  public AsyncOperationBuilder<T> onError(Consumer<Throwable> callback) {
    this.onError = callback;
    return this;
  }

  public void execute() {
    CompletableFuture<?> chain = CompletableFuture.completedFuture(null);

    for (Supplier<CompletableFuture<?>> operation : operations) {
      chain = chain.thenCompose(v -> operation.get());
    }

    if (finalOperation != null) {
      chain
          .thenApplyAsync(v -> finalOperation.get())
          .whenComplete(
              (result, error) -> {
                AsyncExecutor.runOnUIThread(
                    () -> {
                      if (error != null) {
                        if (onError != null) {
                          onError.accept(error);
                        }
                      } else {
                        if (onSuccess != null) {
                          onSuccess.accept(result);
                        }
                      }
                    });
              });
    }
  }

  public static <T> AsyncOperationBuilder<T> create() {
    return new AsyncOperationBuilder<>();
  }
}
