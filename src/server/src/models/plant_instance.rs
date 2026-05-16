use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct PlantInstance {
    pub id: i32,
    pub garden_id: i32,
    pub plant_id: i32,
    pub cell_x: i32,
    pub cell_y: i32,
    pub planted_at: NaiveDateTime,
    pub growth_stage: i32,
    pub is_watered: bool,
    pub is_fertilized: bool,
}
