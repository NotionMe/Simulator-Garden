package ua.notion.domain.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
  private Integer id;
  private Integer plantInstanceId;
  private String taskType;
  private LocalDateTime dueAt;
  private Boolean isDone;
}
