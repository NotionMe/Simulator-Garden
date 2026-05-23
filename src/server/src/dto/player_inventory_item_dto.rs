use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

use crate::models::PlayerInventoryItem;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreatePlayerInventoryItemDto {
    pub user_id: i32,
    pub plant_id: i32,
    pub quantity: Option<i32>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdatePlayerInventoryItemDto {
    pub user_id: Option<i32>,
    pub plant_id: Option<i32>,
    pub quantity: Option<i32>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PlayerInventoryItemResponseDto {
    pub id: i32,
    pub user_id: i32,
    pub plant_id: i32,
    pub quantity: i32,
    pub update_at: NaiveDateTime,
}

impl From<PlayerInventoryItem> for PlayerInventoryItemResponseDto {
    fn from(item: PlayerInventoryItem) -> Self {
        Self {
            id: item.id,
            user_id: item.user_id,
            plant_id: item.plant_id,
            quantity: item.quantity,
            update_at: item.update_at,
        }
    }
}
