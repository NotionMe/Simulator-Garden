use chrono::Utc;

use crate::{
    dto::{CreateWeatherEventDto, UpdateWeatherEventDto, WeatherEventResponseDto},
    errors::error::{DatabaseError, DbResult},
    models::WeatherEvent,
    repositories::weather_event_repository::WeatherEventRepository,
};

pub struct WeatherEventService<R>
where
    R: WeatherEventRepository + Sync,
{
    repo: R,
}

impl<R> WeatherEventService<R>
where
    R: WeatherEventRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_weather_event(
        &self,
        dto: CreateWeatherEventDto,
    ) -> DbResult<WeatherEventResponseDto> {
        if dto.intensity < 0 {
            return Err(DatabaseError::FailedToSave);
        }

        let weather_event = WeatherEvent {
            id: 0,
            garden_id: dto.garden_id,
            event_type: dto.event_type,
            intensity: dto.intensity,
            occurred_at: dto.occurred_at.unwrap_or_else(|| Utc::now()),
        };

        let created = self.repo.create(weather_event).await?;

        Ok(WeatherEventResponseDto::from(created))
    }

    pub async fn get_weather_event(&self, id: i32) -> DbResult<WeatherEventResponseDto> {
        let weather_event = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(WeatherEventResponseDto::from(weather_event))
    }

    pub async fn get_weather_events_by_garden(
        &self,
        garden_id: i32,
    ) -> DbResult<Vec<WeatherEventResponseDto>> {
        let weather_events = self.repo.find_by_garden_id(garden_id).await?;

        Ok(weather_events
            .into_iter()
            .map(WeatherEventResponseDto::from)
            .collect())
    }

    pub async fn get_all_weather_events(&self) -> DbResult<Vec<WeatherEventResponseDto>> {
        let weather_events = self.repo.find_all().await?;

        Ok(weather_events
            .into_iter()
            .map(WeatherEventResponseDto::from)
            .collect())
    }

    pub async fn update_weather_event(
        &self,
        id: i32,
        dto: UpdateWeatherEventDto,
    ) -> DbResult<WeatherEventResponseDto> {
        let mut weather_event = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(garden_id) = dto.garden_id {
            weather_event.garden_id = garden_id;
        }

        if let Some(event_type) = dto.event_type {
            weather_event.event_type = event_type;
        }

        if let Some(intensity) = dto.intensity {
            if intensity < 0 {
                return Err(DatabaseError::FailedToUpdate);
            }
            weather_event.intensity = intensity;
        }

        if let Some(occurred_at) = dto.occurred_at {
            weather_event.occurred_at = occurred_at;
        }

        let updated = self.repo.update(id, weather_event).await?;

        Ok(WeatherEventResponseDto::from(updated))
    }

    pub async fn delete_weather_event(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
