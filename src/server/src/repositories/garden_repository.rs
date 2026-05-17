use async_trait::async_trait;
use sqlx::PgPool;

use crate::{errors::error::DbResult, models::Garden, repositories::repository::Repository};

#[async_trait]
pub trait GardenRepository: Repository<Garden, i32> {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Garden>>;
    async fn find_by_name(&self, name: &str) -> DbResult<Vec<Garden>>;
    async fn find_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<Option<Garden>>;
    async fn exists_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<bool>;
}

pub struct PgGardenRepository {
    pool: PgPool,
}

impl PgGardenRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<Garden, i32> for PgGardenRepository {
    async fn create(&self, object: Garden) -> DbResult<Garden> {
        let sql = r#"
            INSERT INTO gardens (user_id, name, width_cells, height_cells, created_at)
            VALUES ($1, $2, $3, $4, $5)
            RETURNING *"#;

        let inserted = sqlx::query_as::<_, Garden>(sql)
            .bind(object.user_id)
            .bind(object.name)
            .bind(object.width_cells)
            .bind(object.height_cells)
            .bind(object.created_at)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Garden>> {
        let sql = "SELECT * FROM gardens WHERE id = $1";

        let found = sqlx::query_as::<_, Garden>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_all(&self) -> DbResult<Vec<Garden>> {
        let sql = "SELECT * FROM gardens";

        let found = sqlx::query_as::<_, Garden>(sql)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn update(&self, id: i32, object: Garden) -> DbResult<Garden> {
        let sql = r#"
            UPDATE gardens
            SET
                user_id = $1,
                name = $2,
                width_cells = $3,
                height_cells = $4,
                created_at = $5
            WHERE id = $6
            RETURNING *"#;

        let updated = sqlx::query_as::<_, Garden>(sql)
            .bind(object.user_id)
            .bind(object.name)
            .bind(object.width_cells)
            .bind(object.height_cells)
            .bind(object.created_at)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(updated)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM gardens WHERE id = $1";

        let deleted = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        Ok(deleted.rows_affected() == 1)
    }
}

#[async_trait]
impl GardenRepository for PgGardenRepository {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Garden>> {
        let sql = "SELECT * FROM gardens WHERE user_id = $1";

        let found = sqlx::query_as::<_, Garden>(sql)
            .bind(user_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_name(&self, name: &str) -> DbResult<Vec<Garden>> {
        let sql = "SELECT * FROM gardens WHERE name = $1";

        let found = sqlx::query_as::<_, Garden>(sql)
            .bind(name)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<Option<Garden>> {
        let sql = "SELECT * FROM gardens WHERE user_id = $1 AND name = $2";

        let found = sqlx::query_as::<_, Garden>(sql)
            .bind(user_id)
            .bind(name)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn exists_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<bool> {
        let sql = "SELECT EXISTS(SELECT 1 FROM gardens WHERE user_id = $1 AND name = $2)";

        let exists = sqlx::query_scalar::<_, bool>(sql)
            .bind(user_id)
            .bind(name)
            .fetch_one(&self.pool)
            .await?;

        Ok(exists)
    }
}
