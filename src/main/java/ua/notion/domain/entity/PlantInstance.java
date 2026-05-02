package ua.notion.domain.entity;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantInstance {
  private Integer id;
  private Integer gardenId;
  private Integer plantId;
  private Integer cellX;
  private Integer cellY;
  private LocalDate plantedAt;
  private Integer growthStage;
  private Boolean isWatered;
  private Boolean isFertilized;
}
