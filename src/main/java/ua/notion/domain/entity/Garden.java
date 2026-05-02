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
public class Garden {
  private Integer id;
  private Integer userId;
  private String name;
  private Integer widthCells;
  private Integer heightCells;
  private LocalDateTime createdAt;
}
