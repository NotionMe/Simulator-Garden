use axum::async_trait;

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
