use async_trait::async_trait;
use sqlx::PgPool;

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

pub struct PgPlantRepository {
    pool: PgPool,
}

impl PgPlantRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<Plant, i32> for PgPlantRepository {
    async fn create(&self, object: Plant) -> DbResult<Plant> {
        todo!()
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Plant>> {
        todo!()
    }

    async fn find_all(&self) -> DbResult<Vec<Plant>> {
        todo!()
    }

    async fn update(&self, id: i32, object: Plant) -> DbResult<Plant> {
        todo!()
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        todo!()
    }
}

#[async_trait]
impl PlantRepository for PgPlantRepository {
    async fn find_by_name(&self, name: &str) -> DbResult<Vec<Plant>> {
        todo!()
    }

    async fn find_by_species(&self, species: &str) -> DbResult<Vec<Plant>> {
        todo!()
    }

    async fn find_by_climate_type(&self, climate_type: ClimateType) -> DbResult<Vec<Plant>> {
        todo!()
    }

    async fn find_by_growth_days_less_than_or_equal(&self, days: i32) -> DbResult<Vec<Plant>> {
        todo!()
    }

    async fn search_by_name(&self, query: &str) -> DbResult<Vec<Plant>> {
        todo!()
    }
}
