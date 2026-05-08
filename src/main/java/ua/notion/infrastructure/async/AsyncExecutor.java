package ua.notion.infrastructure.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javafx.application.Platform;

public class AsyncExecutor {
  private static int threadCounter = 0;
  private static final ExecutorService executor =
      Executors.newFixedThreadPool(
          Runtime.getRuntime().availableProcessors(),
          r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("AsyncExecutor-" + (++threadCounter));
            return thread;
          });

  public static <T> CompletableFuture<T> runAsync(Supplier<T> task) {
    return CompletableFuture.supplyAsync(task, executor);
  }

  public static CompletableFuture<Void> runAsync(Runnable task) {
    return CompletableFuture.runAsync(task, executor);
  }

  public static <T> AsyncTask<T> execute(Supplier<T> backgroundTask) {
    return new AsyncTask<>(backgroundTask, executor);
  }

  public static void runOnUIThread(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
    } else {
      Platform.runLater(action);
    }
  }

  public static void shutdown() {
    executor.shutdown();
  }
}
