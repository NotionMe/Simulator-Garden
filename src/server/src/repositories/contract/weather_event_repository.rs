use axum::async_trait;
use chrono::{DateTime, Utc};

use crate::{
    errors::error::DbResult,
    models::{EventType, WeatherEvent},
    repositories::repository::Repository,
};

#[async_trait]
pub trait WeatherEventRepository: Repository<WeatherEvent, i32> {
    async fn find_by_garden_id(&self, garden_id: i32) -> DbResult<Vec<WeatherEvent>>;
    async fn find_by_event_type(&self, event_type: EventType) -> DbResult<Vec<WeatherEvent>>;
    async fn find_by_garden_id_and_event_type(
        &self,
        garden_id: i32,
        event_type: EventType,
    ) -> DbResult<Vec<WeatherEvent>>;
    async fn find_by_garden_id_since(
        &self,
        garden_id: i32,
        since: DateTime<Utc>,
    ) -> DbResult<Vec<WeatherEvent>>;
}
