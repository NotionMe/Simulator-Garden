use axum::async_trait;

use crate::{errors::error::DbResult, models::User, repositories::repository::Repository};

#[async_trait]
pub trait UserRepository: Repository<User, i32> {
    async fn find_by_username(&self, username: &str) -> DbResult<Option<User>>;
    async fn find_by_email(&self, email: &str) -> DbResult<Option<User>>;

    async fn exists_by_username(&self, username: &str) -> DbResult<bool>;
    async fn exists_by_email(&self, email: &str) -> DbResult<bool>;
}
