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
public class PlayerInventoryItem {
  private Integer id;
  private Integer userId;
  private Integer plantId;
  private Integer quantity;
  private LocalDateTime updateAt;
}
