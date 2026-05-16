use serde::{Deserialize, Serialize};
use sqlx::FromRow;

use super::enums::ClimateType;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct Plant {
    pub id: i32,
    pub name: String,
    pub species: String,
    pub growth_days: i32,
    pub climate_type: ClimateType,
    pub icon_key: String,
}
