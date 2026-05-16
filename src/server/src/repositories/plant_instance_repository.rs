use async_trait::async_trait;
use sqlx::PgPool;

use crate::{errors::error::DbResult, models::PlantInstance, repositories::repository::Repository};

#[async_trait]
pub trait PlantInstanceRepository: Repository<PlantInstance, i32> {
    async fn find_by_garden_id(&self, garden_id: i32) -> DbResult<Vec<PlantInstance>>;
    async fn find_by_plant_id(&self, plant_id: i32) -> DbResult<Vec<PlantInstance>>;
    async fn find_by_position(
        &self,
        garden_id: i32,
        cell_x: i32,
        cell_y: i32,
    ) -> DbResult<Option<PlantInstance>>;
    async fn exists_at_position(&self, garden_id: i32, cell_x: i32, cell_y: i32) -> DbResult<bool>;
    async fn is_watered(&self, id: i32) -> DbResult<Option<bool>>;
    async fn is_fertilized(&self, id: i32) -> DbResult<Option<bool>>;
    async fn find_growth_stage_by_id(&self, id: i32) -> DbResult<Option<i32>>;
}

pub struct PgPlantInstanceRepository {
    pool: PgPool,
}

impl PgPlantInstanceRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<PlantInstance, i32> for PgPlantInstanceRepository {
    async fn create(&self, object: PlantInstance) -> DbResult<PlantInstance> {
        todo!()
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<PlantInstance>> {
        todo!()
    }

    async fn find_all(&self) -> DbResult<Vec<PlantInstance>> {
        todo!()
    }

    async fn update(&self, id: i32, object: PlantInstance) -> DbResult<PlantInstance> {
        todo!()
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        todo!()
    }
}

#[async_trait]
impl PlantInstanceRepository for PgPlantInstanceRepository {
    async fn find_by_garden_id(&self, garden_id: i32) -> DbResult<Vec<PlantInstance>> {
        todo!()
    }

    async fn find_by_plant_id(&self, plant_id: i32) -> DbResult<Vec<PlantInstance>> {
        todo!()
    }

    async fn find_by_position(
        &self,
        garden_id: i32,
        cell_x: i32,
        cell_y: i32,
    ) -> DbResult<Option<PlantInstance>> {
        todo!()
    }

    async fn exists_at_position(&self, garden_id: i32, cell_x: i32, cell_y: i32) -> DbResult<bool> {
        todo!()
    }

    async fn is_watered(&self, id: i32) -> DbResult<Option<bool>> {
        todo!()
    }

    async fn is_fertilized(&self, id: i32) -> DbResult<Option<bool>> {
        todo!()
    }

    async fn find_growth_stage_by_id(&self, id: i32) -> DbResult<Option<i32>> {
        todo!()
    }
}
