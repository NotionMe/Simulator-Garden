use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

use crate::models::PlantInstance;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreatePlantInstanceDto {
    pub garden_id: i32,
    pub plant_id: i32,
    pub cell_x: i32,
    pub cell_y: i32,
    pub growth_stage: Option<i32>,
    pub is_watered: Option<bool>,
    pub is_fertilized: Option<bool>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdatePlantInstanceDto {
    pub garden_id: Option<i32>,
    pub plant_id: Option<i32>,
    pub cell_x: Option<i32>,
    pub cell_y: Option<i32>,
    pub planted_at: Option<NaiveDateTime>,
    pub growth_stage: Option<i32>,
    pub is_watered: Option<bool>,
    pub is_fertilized: Option<bool>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PlantInstanceResponseDto {
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

impl From<PlantInstance> for PlantInstanceResponseDto {
    fn from(plant_instance: PlantInstance) -> Self {
        Self {
            id: plant_instance.id,
            garden_id: plant_instance.garden_id,
            plant_id: plant_instance.plant_id,
            cell_x: plant_instance.cell_x,
            cell_y: plant_instance.cell_y,
            planted_at: plant_instance.planted_at,
            growth_stage: plant_instance.growth_stage,
            is_watered: plant_instance.is_watered,
            is_fertilized: plant_instance.is_fertilized,
        }
    }
}
