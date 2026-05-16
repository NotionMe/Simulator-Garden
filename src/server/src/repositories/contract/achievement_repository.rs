use axum::async_trait;

use crate::{errors::error::DbResult, models::Achievement, repositories::repository::Repository};

#[async_trait]
pub trait AchievementRepository: Repository<Achievement, i32> {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Achievement>>;
    async fn find_by_title(&self, title: &str) -> DbResult<Vec<Achievement>>;
    async fn find_by_condition_key(&self, condition_key: &str) -> DbResult<Vec<Achievement>>;
    async fn exists_by_user_id_and_condition_key(&self, user_id: i32, condition_key: &str) -> DbResult<bool>;
}
