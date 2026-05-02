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
public class WeatherEvent {
  private Integer id;
  private Integer gardenId;
  private String eventType;
  private Integer intensity;
  private LocalDateTime occurredAt;
}
