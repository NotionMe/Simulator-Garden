package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.Task;
import ua.notion.infrastructure.persistence.Repository;

public interface TaskRepository extends Repository<Task, Integer> {

  List<Task> findByPlantInstanceId(Integer plantInstanceId);

  List<Task> findByTaskType(String taskType);

  List<Task> findPendingTasks();

  long countByPlantInstanceId(Integer plantInstanceId);
}
