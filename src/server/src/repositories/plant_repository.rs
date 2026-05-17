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
        let sql = r#"
            INSERT INTO plants (name, species, growth_days, climate_type, icon_key)
            VALUES ($1, $2, $3, $4, $5)
            RETURNING *"#;

        let inserted = sqlx::query_as::<_, Plant>(sql)
            .bind(object.name)
            .bind(object.species)
            .bind(object.growth_days)
            .bind(object.climate_type)
            .bind(object.icon_key)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Plant>> {
        let sql = "SELECT * FROM plants WHERE id = $1";

        let found = sqlx::query_as::<_, Plant>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_all(&self) -> DbResult<Vec<Plant>> {
        let sql = "SELECT * FROM plants";

        let found = sqlx::query_as::<_, Plant>(sql)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn update(&self, id: i32, object: Plant) -> DbResult<Plant> {
        let sql = r#"
            UPDATE plants
            SET
                name = $1,
                species = $2,
                growth_days = $3,
                climate_type = $4,
                icon_key = $5
            WHERE id = $6
            RETURNING *"#;

        let updated = sqlx::query_as::<_, Plant>(sql)
            .bind(object.name)
            .bind(object.species)
            .bind(object.growth_days)
            .bind(object.climate_type)
            .bind(object.icon_key)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(updated)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM plants WHERE id = $1";

        let deleted = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        Ok(deleted.rows_affected() == 1)
    }
}

#[async_trait]
impl PlantRepository for PgPlantRepository {
    async fn find_by_name(&self, name: &str) -> DbResult<Vec<Plant>> {
        let sql = "SELECT * FROM plants WHERE name = $1";

        let found = sqlx::query_as::<_, Plant>(sql)
            .bind(name)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_species(&self, species: &str) -> DbResult<Vec<Plant>> {
        let sql = "SELECT * FROM plants WHERE species = $1";

        let found = sqlx::query_as::<_, Plant>(sql)
            .bind(species)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_climate_type(&self, climate_type: ClimateType) -> DbResult<Vec<Plant>> {
        let sql = "SELECT * FROM plants WHERE climate_type = $1";

        let found = sqlx::query_as::<_, Plant>(sql)
            .bind(climate_type)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_growth_days_less_than_or_equal(&self, days: i32) -> DbResult<Vec<Plant>> {
        let sql = "SELECT * FROM plants WHERE growth_days <= $1";

        let found = sqlx::query_as::<_, Plant>(sql)
            .bind(days)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn search_by_name(&self, query: &str) -> DbResult<Vec<Plant>> {
        let sql = "SELECT * FROM plants WHERE name ILIKE $1";
        let pattern = format!("%{}%", query);

        let found = sqlx::query_as::<_, Plant>(sql)
            .bind(pattern)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }
}
