package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.PlayerInventoryItem;
import ua.notion.infrastructure.persistence.Repository;

public interface PlayerInventoryItemRepository extends Repository<PlayerInventoryItem, Integer> {

  List<PlayerInventoryItem> findByUserId(Integer userId);
}
