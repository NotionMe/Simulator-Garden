use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct Garden {
    pub id: i32,
    pub user_id: i32,
    pub name: String,
    pub width_cells: i32,
    pub height_cells: i32,
    pub created_at: DateTime<Utc>,
}
