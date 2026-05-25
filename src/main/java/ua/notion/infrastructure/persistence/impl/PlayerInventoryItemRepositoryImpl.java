package ua.notion.infrastructure.persistence.impl;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import ua.notion.domain.entity.PlayerInventoryItem;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.PlayerInventoryItemRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class PlayerInventoryItemRepositoryImpl
    extends GenericRepository<PlayerInventoryItem, Integer>
    implements PlayerInventoryItemRepository {

  public PlayerInventoryItemRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, PlayerInventoryItem.class, "playerinventoryitem");
  }

  @Override
  public List<PlayerInventoryItem> findByUserId(Integer userId) {
    Type listType = TypeToken.getParameterized(List.class, PlayerInventoryItem.class).getType();
    List<PlayerInventoryItem> result =
        apiClient.send("list", resourceName, Map.of("user_id", userId), listType);
    return result != null ? result : List.of();
  }
}
