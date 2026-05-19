use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use crate::models::{EventType, WeatherEvent};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreateWeatherEventDto {
    pub garden_id: i32,
    pub event_type: EventType,
    pub intensity: i32,
    pub occurred_at: Option<DateTime<Utc>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdateWeatherEventDto {
    pub garden_id: Option<i32>,
    pub event_type: Option<EventType>,
    pub intensity: Option<i32>,
    pub occurred_at: Option<DateTime<Utc>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WeatherEventResponseDto {
    pub id: i32,
    pub garden_id: i32,
    pub event_type: EventType,
    pub intensity: i32,
    pub occurred_at: DateTime<Utc>,
}

impl From<WeatherEvent> for WeatherEventResponseDto {
    fn from(weather_event: WeatherEvent) -> Self {
        Self {
            id: weather_event.id,
            garden_id: weather_event.garden_id,
            event_type: weather_event.event_type,
            intensity: weather_event.intensity,
            occurred_at: weather_event.occurred_at,
        }
    }
}
