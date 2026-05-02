package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.Garden;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class GardenRepositoryImpl extends GenericRepository<Garden, Integer>
    implements GardenRepository {

  public GardenRepositoryImpl(ConnectionPool connectionPool) {
    super(connectionPool, Garden.class, "gardens");
  }

  @Override
  public List<Garden> findByUserId(Integer userId) {
    return findByField("user_id", userId);
  }

  @Override
  public long countByUserId(Integer userId) {
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("user_id = ?");
          params.add(userId);
        };
    return count(filter);
  }
}
