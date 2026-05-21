package ua.notion.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ua.notion.domain.entity.Garden;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.GardenRepository;

public class GardenService {
  private final PersistenceContext context;
  private final GardenRepository gardenRepository;

  public GardenService(PersistenceContext context) {
    this.context = context;
    this.gardenRepository = context.getGardenRepository();
  }

  public Garden createGarden(Integer userId, String name, Integer widthCells, Integer heightCells) {
    if (widthCells <= 0 || heightCells <= 0) {
      throw new IllegalArgumentException("Garden dimensions must be positive");
    }

    Garden garden =
        Garden.builder()
            .userId(userId)
            .name(name)
            .widthCells(widthCells)
            .heightCells(heightCells)
            .createdAt(LocalDateTime.now())
            .build();

    try {
      return gardenRepository.save(garden);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create garden", e);
    }
  }

  public Optional<Garden> findGardenById(Integer id) {
    return gardenRepository.findById(id);
  }

  public List<Garden> findGardensByUserId(Integer userId) {
    return gardenRepository.findAll().stream().filter(g -> g.getUserId().equals(userId)).toList();
  }

  public List<Garden> getAllGardens() {
    return gardenRepository.findAll();
  }

  public void updateGarden(Garden garden) {
    if (garden.getId() == null) {
      throw new IllegalArgumentException("Garden ID cannot be null");
    }
    if (garden.getWidthCells() <= 0 || garden.getHeightCells() <= 0) {
      throw new IllegalArgumentException("Garden dimensions must be positive");
    }

    try {
      gardenRepository.update(garden.getId(), garden);
    } catch (Exception e) {
      throw new RuntimeException("Failed to update garden", e);
    }
  }

  public void deleteGarden(Integer id) {
    try {
      gardenRepository.delete(id);
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete garden", e);
    }
  }

  public int getTotalCells(Garden garden) {
    return garden.getWidthCells() * garden.getHeightCells();
  }
}
