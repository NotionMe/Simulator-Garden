use serde::{Deserialize, Serialize};
use sqlx::Type;

#[derive(Debug, Clone, Serialize, Deserialize, Type)]
#[serde(rename_all = "snake_case")]
#[sqlx(type_name = "text", rename_all = "snake_case")]
pub enum TaskType {
    Water,
    Fertilize,
    Prune,
    Harvest,
    PestControl,
}

#[derive(Debug, Clone, Serialize, Deserialize, Type)]
#[serde(rename_all = "snake_case")]
#[sqlx(type_name = "text", rename_all = "snake_case")]
pub enum ClimateType {
    Tropical,
    Temperate,
    Arid,
    Cold,
    Mediterranean,
}

#[derive(Debug, Clone, Serialize, Deserialize, Type)]
#[serde(rename_all = "snake_case")]
#[sqlx(type_name = "text", rename_all = "snake_case")]
pub enum EventType {
    Rain,
    Sun,
    Storm,
    Frost,
    Drought,
    Heatwave,
    Snow,
}
