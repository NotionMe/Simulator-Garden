use axum::async_trait;

use crate::{errors::error::DbResult, models::Garden, repositories::repository::Repository};

#[async_trait]
pub trait GardenRepository: Repository<Garden, i32> {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Garden>>;
    async fn find_by_name(&self, name: &str) -> DbResult<Vec<Garden>>;
    async fn find_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<Option<Garden>>;
    async fn exists_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<bool>;
}
