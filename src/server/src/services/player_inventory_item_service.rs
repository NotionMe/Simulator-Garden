use chrono::Utc;

use crate::{
    dto::{
        CreatePlayerInventoryItemDto, PlayerInventoryItemResponseDto, UpdatePlayerInventoryItemDto,
    },
    errors::error::{DatabaseError, DbResult},
    models::PlayerInventoryItem,
    repositories::player_inventory_item_repository::PlayerInventoryItemRepository,
};

pub struct PlayerInventoryItemService<R>
where
    R: PlayerInventoryItemRepository + Sync,
{
    repo: R,
}

impl<R> PlayerInventoryItemService<R>
where
    R: PlayerInventoryItemRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_item(
        &self,
        dto: CreatePlayerInventoryItemDto,
    ) -> DbResult<PlayerInventoryItemResponseDto> {
        let quantity = dto.quantity.unwrap_or(1);
        if quantity <= 0 {
            return Err(DatabaseError::FailedToSave);
        }

        let item = PlayerInventoryItem {
            id: 0,
            user_id: dto.user_id,
            plant_id: dto.plant_id,
            quantity,
            update_at: Utc::now().naive_utc(),
        };

        let created = self.repo.create(item).await?;

        Ok(PlayerInventoryItemResponseDto::from(created))
    }

    pub async fn get_item(&self, id: i32) -> DbResult<PlayerInventoryItemResponseDto> {
        let item = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(PlayerInventoryItemResponseDto::from(item))
    }

    pub async fn get_items_by_user(
        &self,
        user_id: i32,
    ) -> DbResult<Vec<PlayerInventoryItemResponseDto>> {
        let items = self.repo.find_by_user_id(user_id).await?;

        Ok(items
            .into_iter()
            .map(PlayerInventoryItemResponseDto::from)
            .collect())
    }

    pub async fn get_all_items(&self) -> DbResult<Vec<PlayerInventoryItemResponseDto>> {
        let items = self.repo.find_all().await?;

        Ok(items
            .into_iter()
            .map(PlayerInventoryItemResponseDto::from)
            .collect())
    }

    pub async fn update_item(
        &self,
        id: i32,
        dto: UpdatePlayerInventoryItemDto,
    ) -> DbResult<PlayerInventoryItemResponseDto> {
        let mut item = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(user_id) = dto.user_id {
            item.user_id = user_id;
        }

        if let Some(plant_id) = dto.plant_id {
            item.plant_id = plant_id;
        }

        if let Some(quantity) = dto.quantity {
            if quantity <= 0 {
                return Err(DatabaseError::FailedToUpdate);
            }
            item.quantity = quantity;
        }

        item.update_at = Utc::now().naive_utc();

        let updated = self.repo.update(id, item).await?;

        Ok(PlayerInventoryItemResponseDto::from(updated))
    }

    pub async fn delete_item(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
