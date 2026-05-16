use axum::async_trait;

use crate::{
    errors::error::DbResult,
    models::{ClimateType, Plant},
    repositories::repository::Repository,
};

#[async_trait]
pub trait PlantRepository: Repository<Plant, i32> {
    async fn find_by_name(&self, name: &str) -> DbResult<Vec<Plant>>;
    async fn find_by_species(&self, species: &str) -> DbResult<Vec<Plant>>;
    async fn find_by_climate_type(&self, climate_type: ClimateType) -> DbResult<Vec<Plant>>;
    async fn find_by_growth_days_less_than_or_equal(&self, days: i32) -> DbResult<Vec<Plant>>;
    async fn search_by_name(&self, query: &str) -> DbResult<Vec<Plant>>;
}
