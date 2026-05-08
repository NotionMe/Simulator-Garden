package ua.notion.infrastructure.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;

public class AsyncTask<T> {
  private final CompletableFuture<T> future;
  private Consumer<T> onSuccess;
  private Consumer<Throwable> onError;
  private Runnable onComplete;

  public AsyncTask(Supplier<T> task, ExecutorService executor) {
    this.future = CompletableFuture.supplyAsync(task, executor);
  }

  public AsyncTask<T> onSuccess(Consumer<T> callback) {
    this.onSuccess = callback;
    return this;
  }

  public AsyncTask<T> onError(Consumer<Throwable> callback) {
    this.onError = callback;
    return this;
  }

  public AsyncTask<T> onComplete(Runnable callback) {
    this.onComplete = callback;
    return this;
  }

  public void execute() {
    future.whenComplete(
        (result, error) -> {
          Platform.runLater(
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

                if (onComplete != null) {
                  onComplete.run();
                }
              });
        });
  }

  public CompletableFuture<T> getFuture() {
    return future;
  }

  public void cancel() {
    future.cancel(true);
  }
}
