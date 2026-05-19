use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct IdDto {
    pub id: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserIdDto {
    pub user_id: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GardenIdDto {
    pub garden_id: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PlantIdDto {
    pub plant_id: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PlantInstanceIdDto {
    pub plant_instance_id: i32,
}
