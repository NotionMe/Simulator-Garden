package ua.notion.infrastructure.async;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import ua.notion.infrastructure.persistence.Repository;

public class AsyncRepository<T, ID> {
  private final Repository<T, ID> repository;

  public AsyncRepository(Repository<T, ID> repository) {
    this.repository = repository;
  }

  public AsyncTask<Optional<T>> findByIdAsync(ID id) {
    return AsyncExecutor.execute(() -> repository.findById(id));
  }

  public AsyncTask<List<T>> findAllAsync() {
    return AsyncExecutor.execute(() -> repository.findAll());
  }

  public AsyncTask<T> saveAsync(T entity) {
    return AsyncExecutor.execute(() -> repository.save(entity));
  }

  public AsyncTask<Void> deleteAsync(ID id) {
    return AsyncExecutor.execute(
        () -> {
          repository.delete(id);
          return null;
        });
  }

  public AsyncTask<List<T>> findByFilterAsync(Repository.Filter filter) {
    return AsyncExecutor.execute(
        () -> repository.findAll(filter, null, true, 0, Integer.MAX_VALUE));
  }

  public void findByIdWithCallback(
      ID id, Consumer<Optional<T>> onSuccess, Consumer<Throwable> onError) {
    findByIdAsync(id).onSuccess(onSuccess).onError(onError).execute();
  }

  public void saveWithCallback(T entity, Consumer<T> onSuccess, Consumer<Throwable> onError) {
    saveAsync(entity).onSuccess(onSuccess).onError(onError).execute();
  }
}
