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
        todo!()
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Achievement>> {
        todo!()
    }

    async fn find_all(&self) -> DbResult<Vec<Achievement>> {
        todo!()
    }

    async fn update(&self, id: i32, object: Achievement) -> DbResult<Achievement> {
        todo!()
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        todo!()
    }
}

#[async_trait]
impl AchievementRepository for PgAchievementRepository {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Achievement>> {
        todo!()
    }

    async fn find_by_title(&self, title: &str) -> DbResult<Vec<Achievement>> {
        todo!()
    }

    async fn find_by_condition_key(&self, condition_key: &str) -> DbResult<Vec<Achievement>> {
        todo!()
    }

    async fn exists_by_user_id_and_condition_key(
        &self,
        user_id: i32,
        condition_key: &str,
    ) -> DbResult<bool> {
        todo!()
    }
}
