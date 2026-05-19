use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use crate::models::Garden;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreateGardenDto {
    pub user_id: i32,
    pub name: String,
    pub width_cells: i32,
    pub height_cells: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdateGardenDto {
    pub user_id: Option<i32>,
    pub name: Option<String>,
    pub width_cells: Option<i32>,
    pub height_cells: Option<i32>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GardenResponseDto {
    pub id: i32,
    pub user_id: i32,
    pub name: String,
    pub width_cells: i32,
    pub height_cells: i32,
    pub created_at: DateTime<Utc>,
}

impl From<Garden> for GardenResponseDto {
    fn from(garden: Garden) -> Self {
        Self {
            id: garden.id,
            user_id: garden.user_id,
            name: garden.name,
            width_cells: garden.width_cells,
            height_cells: garden.height_cells,
            created_at: garden.created_at,
        }
    }
}
