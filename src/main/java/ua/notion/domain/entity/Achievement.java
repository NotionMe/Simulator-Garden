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
public class Achievement {
  private Integer id;
  private Integer userId;
  private String title;
  private String conditionKey;
  private LocalDateTime earnedAt;
}
