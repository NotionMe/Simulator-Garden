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
        let sql = "INSERT INTO weather_events (garden_id, event_type, intensity, occurred_at) VALUES ($1,$2,$3,$4) RETURNING *";

        let inserted = sqlx::query_as::<_, WeatherEvent>(sql)
            .bind(object.garden_id)
            .bind(object.event_type)
            .bind(object.intensity)
            .bind(object.occurred_at)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_all(&self) -> DbResult<Vec<WeatherEvent>> {
        let sql = "SELECT * FROM weather_events";

        let find = sqlx::query_as::<_, WeatherEvent>(sql)
            .fetch_all(&self.pool)
            .await?;

        Ok(find)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<WeatherEvent>> {
        let sql = "SELECT * FROM weather_events WHERE id = $1";

        let find = sqlx::query_as::<_, WeatherEvent>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(find)
    }

    async fn update(&self, id: i32, object: WeatherEvent) -> DbResult<WeatherEvent> {
        let sql = r#"
            UPDATE weather_events
                SET
                    garden_id =  $1,
                    event_type =  $2,
                    intensity =  $3,
                    occurred_at = $4
                WHERE id = $5
                RETURNING *"#;

        let update = sqlx::query_as::<_, WeatherEvent>(sql)
            .bind(object.garden_id)
            .bind(object.event_type)
            .bind(object.intensity)
            .bind(object.occurred_at)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(update)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM weather_events WHERE id = $1";

        let delete = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        Ok(delete.rows_affected() == 1)
    }
}

#[async_trait]
impl WeatherEventRepository for PgWeatherEventRepository {
    async fn find_by_garden_id(&self, garden_id: i32) -> DbResult<Vec<WeatherEvent>> {
        let sql = "SELECT * FROM weather_events WHERE garden_id = $1";

        let find = sqlx::query_as::<_, WeatherEvent>(sql)
            .bind(garden_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(find)
    }

    async fn find_by_event_type(&self, event_type: EventType) -> DbResult<Vec<WeatherEvent>> {
        let sql = "SELECT * FROM weather_events WHERE event_type = $1";

        let find = sqlx::query_as::<_, WeatherEvent>(sql)
            .bind(event_type)
            .fetch_all(&self.pool)
            .await?;

        Ok(find)
    }

    async fn find_by_garden_id_and_event_type(
        &self,
        garden_id: i32,
        event_type: EventType,
    ) -> DbResult<Vec<WeatherEvent>> {
        let sql = "SELECT * FROM weather_events WHERE garden_id = $1 AND event_type = $2";

        let find = sqlx::query_as::<_, WeatherEvent>(sql)
            .bind(garden_id)
            .bind(event_type)
            .fetch_all(&self.pool)
            .await?;

        Ok(find)
    }

    async fn find_by_garden_id_since(
        &self,
        garden_id: i32,
        since: DateTime<Utc>,
    ) -> DbResult<Vec<WeatherEvent>> {
        let sql = "SELECT * FROM weather_events WHERE garden_id = $1 AND occurred_at >= $2";

        let find = sqlx::query_as::<_, WeatherEvent>(sql)
            .bind(garden_id)
            .bind(since)
            .fetch_all(&self.pool)
            .await?;

        Ok(find)
    }
}
