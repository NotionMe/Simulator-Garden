package ua.notion.infrastructure.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {
  private static int threadCounter = 0;
  private static final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(
          2,
          r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("TaskScheduler-" + (++threadCounter));
            return thread;
          });

  public static ScheduledFuture<?> scheduleOnce(Runnable task, long delay, TimeUnit unit) {
    return scheduler.schedule(task, delay, unit);
  }

  public static ScheduledFuture<?> scheduleRepeating(
      Runnable task, long initialDelay, long period, TimeUnit unit) {
    return scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
  }

  public static ScheduledFuture<?> scheduleWithFixedDelay(
      Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return scheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
  }

  public static void shutdown() {
    scheduler.shutdown();
  }
}
