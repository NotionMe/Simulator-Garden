use async_trait::async_trait;
use sqlx::PgPool;

use crate::{errors::error::DbResult, models::Achievement, repositories::repository::Repository};

#[async_trait]
pub trait AchievementRepository: Repository<Achievement, i32> {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Achievement>>;
    async fn find_by_title(&self, title: &str) -> DbResult<Vec<Achievement>>;
    async fn find_by_condition_key(&self, condition_key: &str) -> DbResult<Vec<Achievement>>;
    async fn exists_by_user_id_and_condition_key(
        &self,
        user_id: i32,
        condition_key: &str,
    ) -> DbResult<bool>;
}

pub struct PgAchievementRepository {
    pool: PgPool,
}

impl PgAchievementRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<Achievement, i32> for PgAchievementRepository {
    async fn create(&self, object: Achievement) -> DbResult<Achievement> {
        let sql = r#"
            INSERT INTO achievements (user_id, title, condition_key, earned_at)
            VALUES ($1, $2, $3, $4)
            RETURNING *"#;

        let inserted = sqlx::query_as::<_, Achievement>(sql)
            .bind(object.user_id)
            .bind(object.title)
            .bind(object.condition_key)
            .bind(object.earned_at)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Achievement>> {
        let sql = "SELECT * FROM achievements WHERE id = $1";

        let found = sqlx::query_as::<_, Achievement>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_all(&self) -> DbResult<Vec<Achievement>> {
        let sql = "SELECT * FROM achievements";

        let found = sqlx::query_as::<_, Achievement>(sql)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn update(&self, id: i32, object: Achievement) -> DbResult<Achievement> {
        let sql = r#"
            UPDATE achievements
            SET
                user_id = $1,
                title = $2,
                condition_key = $3,
                earned_at = $4
            WHERE id = $5
            RETURNING *"#;

        let updated = sqlx::query_as::<_, Achievement>(sql)
            .bind(object.user_id)
            .bind(object.title)
            .bind(object.condition_key)
            .bind(object.earned_at)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(updated)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM achievements WHERE id = $1";

        let deleted = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        Ok(deleted.rows_affected() == 1)
    }
}

#[async_trait]
impl AchievementRepository for PgAchievementRepository {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Achievement>> {
        let sql = "SELECT * FROM achievements WHERE user_id = $1";

        let found = sqlx::query_as::<_, Achievement>(sql)
            .bind(user_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_title(&self, title: &str) -> DbResult<Vec<Achievement>> {
        let sql = "SELECT * FROM achievements WHERE title = $1";

        let found = sqlx::query_as::<_, Achievement>(sql)
            .bind(title)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_condition_key(&self, condition_key: &str) -> DbResult<Vec<Achievement>> {
        let sql = "SELECT * FROM achievements WHERE condition_key = $1";

        let found = sqlx::query_as::<_, Achievement>(sql)
            .bind(condition_key)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn exists_by_user_id_and_condition_key(
        &self,
        user_id: i32,
        condition_key: &str,
    ) -> DbResult<bool> {
        let sql =
            "SELECT EXISTS(SELECT 1 FROM achievements WHERE user_id = $1 AND condition_key = $2)";

        let exists = sqlx::query_scalar::<_, bool>(sql)
            .bind(user_id)
            .bind(condition_key)
            .fetch_one(&self.pool)
            .await?;

        Ok(exists)
    }
}
