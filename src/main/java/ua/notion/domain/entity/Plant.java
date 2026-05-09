package ua.notion.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plant {
  private Integer id;
  private String name;
  private String firstName;
  private String lastName;
  private String species;
  private Integer growthDays;
  private String climateType;
  private String iconKey;
}
