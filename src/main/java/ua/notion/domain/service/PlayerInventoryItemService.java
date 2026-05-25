package ua.notion.domain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ua.notion.domain.entity.PlayerInventoryItem;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.PlayerInventoryItemRepository;

public class PlayerInventoryItemService {

  private final PlayerInventoryItemRepository repository;

  public PlayerInventoryItemService(PersistenceContext context) {
    this.repository = context.getPlayerInventoryItemRepository();
  }

  public List<PlayerInventoryItem> findByUserId(int userId) {
    return repository.findByUserId(userId);
  }

  public Optional<PlayerInventoryItem> findByUserAndPlant(int userId, int plantId) {
    return findByUserId(userId).stream().filter(item -> plantId == item.getPlantId()).findFirst();
  }

  public PlayerInventoryItem addQuantity(int userId, int plantId, int delta) {
    if (delta <= 0) {
      throw new IllegalArgumentException("delta must be positive");
    }

    Optional<PlayerInventoryItem> existing = findByUserAndPlant(userId, plantId);
    if (existing.isPresent()) {
      PlayerInventoryItem item = existing.get();
      item.setQuantity(item.getQuantity() + delta);
      repository.update(item.getId(), item);
      return item;
    }

    PlayerInventoryItem created =
        PlayerInventoryItem.builder().userId(userId).plantId(plantId).quantity(delta).build();
    return repository.save(created);
  }

  public boolean removeQuantity(int userId, int plantId, int delta) {
    if (delta <= 0) {
      return false;
    }

    Optional<PlayerInventoryItem> existing = findByUserAndPlant(userId, plantId);
    if (existing.isEmpty() || existing.get().getQuantity() < delta) {
      return false;
    }

    PlayerInventoryItem item = existing.get();
    int remaining = item.getQuantity() - delta;
    if (remaining <= 0) {
      repository.delete(item.getId());
      return true;
    }

    item.setQuantity(remaining);
    repository.update(item.getId(), item);
    return true;
  }

  /** Merges duplicate user/plant rows into one (keeps highest quantity row). */
  public Map<Integer, Integer> quantitiesByPlantId(int userId) {
    Map<Integer, Integer> totals = new HashMap<>();
    for (PlayerInventoryItem item : findByUserId(userId)) {
      totals.merge(item.getPlantId(), item.getQuantity(), Integer::sum);
    }
    return totals;
  }

  public void syncQuantity(int userId, int plantId, int targetQuantity) {
    if (targetQuantity < 0) {
      throw new IllegalArgumentException("quantity cannot be negative");
    }

    Optional<PlayerInventoryItem> existing = findByUserAndPlant(userId, plantId);
    if (targetQuantity == 0) {
      existing.ifPresent(item -> repository.delete(item.getId()));
      return;
    }

    if (existing.isPresent()) {
      PlayerInventoryItem item = existing.get();
      item.setQuantity(targetQuantity);
      repository.update(item.getId(), item);
      return;
    }

    repository.save(
        PlayerInventoryItem.builder()
            .userId(userId)
            .plantId(plantId)
            .quantity(targetQuantity)
            .build());
  }
}
