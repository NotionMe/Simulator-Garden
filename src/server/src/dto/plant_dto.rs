use serde::{Deserialize, Serialize};

use crate::models::{ClimateType, Plant};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreatePlantDto {
    pub name: String,
    pub species: String,
    pub growth_days: i32,
    pub climate_type: ClimateType,
    pub icon_key: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdatePlantDto {
    pub name: Option<String>,
    pub species: Option<String>,
    pub growth_days: Option<i32>,
    pub climate_type: Option<ClimateType>,
    pub icon_key: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PlantResponseDto {
    pub id: i32,
    pub name: String,
    pub species: String,
    pub growth_days: i32,
    pub climate_type: ClimateType,
    pub icon_key: String,
}

impl From<Plant> for PlantResponseDto {
    fn from(plant: Plant) -> Self {
        Self {
            id: plant.id,
            name: plant.name,
            species: plant.species,
            growth_days: plant.growth_days,
            climate_type: plant.climate_type,
            icon_key: plant.icon_key,
        }
    }
}
