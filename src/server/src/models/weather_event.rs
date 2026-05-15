use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

use super::enums::EventType;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct WeatherEvent {
    pub id: i32,
    pub garden_id: i32,
    pub event_type: EventType,
    pub intensity: i32,
    pub occurred_at: DateTime<Utc>,
}
