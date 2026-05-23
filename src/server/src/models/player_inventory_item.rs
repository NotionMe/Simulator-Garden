use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct PlayerInventoryItem {
    pub id: i32,
    pub user_id: i32,
    pub plant_id: i32,
    pub quantity: i32,
    pub update_at: NaiveDateTime,
}
