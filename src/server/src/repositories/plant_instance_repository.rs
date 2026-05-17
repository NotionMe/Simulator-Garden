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
        let sql = r#"
            INSERT INTO plant_instances (
                garden_id,
                plant_id,
                cell_x,
                cell_y,
                planted_at,
                growth_stage,
                is_watered,
                is_fertilized
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
            RETURNING *"#;

        let inserted = sqlx::query_as::<_, PlantInstance>(sql)
            .bind(object.garden_id)
            .bind(object.plant_id)
            .bind(object.cell_x)
            .bind(object.cell_y)
            .bind(object.planted_at)
            .bind(object.growth_stage)
            .bind(object.is_watered)
            .bind(object.is_fertilized)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<PlantInstance>> {
        let sql = "SELECT * FROM plant_instances WHERE id = $1";

        let found = sqlx::query_as::<_, PlantInstance>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_all(&self) -> DbResult<Vec<PlantInstance>> {
        let sql = "SELECT * FROM plant_instances";

        let found = sqlx::query_as::<_, PlantInstance>(sql)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn update(&self, id: i32, object: PlantInstance) -> DbResult<PlantInstance> {
        let sql = r#"
            UPDATE plant_instances
            SET
                garden_id = $1,
                plant_id = $2,
                cell_x = $3,
                cell_y = $4,
                planted_at = $5,
                growth_stage = $6,
                is_watered = $7,
                is_fertilized = $8
            WHERE id = $9
            RETURNING *"#;

        let updated = sqlx::query_as::<_, PlantInstance>(sql)
            .bind(object.garden_id)
            .bind(object.plant_id)
            .bind(object.cell_x)
            .bind(object.cell_y)
            .bind(object.planted_at)
            .bind(object.growth_stage)
            .bind(object.is_watered)
            .bind(object.is_fertilized)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(updated)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM plant_instances WHERE id = $1";

        let deleted = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        Ok(deleted.rows_affected() == 1)
    }
}

#[async_trait]
impl PlantInstanceRepository for PgPlantInstanceRepository {
    async fn find_by_garden_id(&self, garden_id: i32) -> DbResult<Vec<PlantInstance>> {
        let sql = "SELECT * FROM plant_instances WHERE garden_id = $1";

        let found = sqlx::query_as::<_, PlantInstance>(sql)
            .bind(garden_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_plant_id(&self, plant_id: i32) -> DbResult<Vec<PlantInstance>> {
        let sql = "SELECT * FROM plant_instances WHERE plant_id = $1";

        let found = sqlx::query_as::<_, PlantInstance>(sql)
            .bind(plant_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_position(
        &self,
        garden_id: i32,
        cell_x: i32,
        cell_y: i32,
    ) -> DbResult<Option<PlantInstance>> {
        let sql = "SELECT * FROM plant_instances WHERE garden_id = $1 AND cell_x = $2 AND cell_y = $3";

        let found = sqlx::query_as::<_, PlantInstance>(sql)
            .bind(garden_id)
            .bind(cell_x)
            .bind(cell_y)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn exists_at_position(&self, garden_id: i32, cell_x: i32, cell_y: i32) -> DbResult<bool> {
        let sql = "SELECT EXISTS(SELECT 1 FROM plant_instances WHERE garden_id = $1 AND cell_x = $2 AND cell_y = $3)";

        let exists = sqlx::query_scalar::<_, bool>(sql)
            .bind(garden_id)
            .bind(cell_x)
            .bind(cell_y)
            .fetch_one(&self.pool)
            .await?;

        Ok(exists)
    }

    async fn is_watered(&self, id: i32) -> DbResult<Option<bool>> {
        let sql = "SELECT is_watered FROM plant_instances WHERE id = $1";

        let is_watered = sqlx::query_scalar::<_, bool>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(is_watered)
    }

    async fn is_fertilized(&self, id: i32) -> DbResult<Option<bool>> {
        let sql = "SELECT is_fertilized FROM plant_instances WHERE id = $1";

        let is_fertilized = sqlx::query_scalar::<_, bool>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(is_fertilized)
    }

    async fn find_growth_stage_by_id(&self, id: i32) -> DbResult<Option<i32>> {
        let sql = "SELECT growth_stage FROM plant_instances WHERE id = $1";

        let growth_stage = sqlx::query_scalar::<_, i32>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(growth_stage)
    }
}
