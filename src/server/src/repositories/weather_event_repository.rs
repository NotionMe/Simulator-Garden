use async_trait::async_trait;
use chrono::{DateTime, Utc};
use sqlx::PgPool;

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

pub struct PgWeatherEventRepository {
    pool: PgPool,
}

impl PgWeatherEventRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<WeatherEvent, i32> for PgWeatherEventRepository {
    async fn create(&self, object: WeatherEvent) -> DbResult<WeatherEvent> {
        todo!()
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<WeatherEvent>> {
        todo!()
    }

    async fn find_all(&self) -> DbResult<Vec<WeatherEvent>> {
        todo!()
    }

    async fn update(&self, id: i32, object: WeatherEvent) -> DbResult<WeatherEvent> {
        todo!()
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        todo!()
    }
}

#[async_trait]
impl WeatherEventRepository for PgWeatherEventRepository {
    async fn find_by_garden_id(&self, garden_id: i32) -> DbResult<Vec<WeatherEvent>> {
        todo!()
    }

    async fn find_by_event_type(&self, event_type: EventType) -> DbResult<Vec<WeatherEvent>> {
        todo!()
    }

    async fn find_by_garden_id_and_event_type(
        &self,
        garden_id: i32,
        event_type: EventType,
    ) -> DbResult<Vec<WeatherEvent>> {
        todo!()
    }

    async fn find_by_garden_id_since(
        &self,
        garden_id: i32,
        since: DateTime<Utc>,
    ) -> DbResult<Vec<WeatherEvent>> {
        todo!()
    }
}
