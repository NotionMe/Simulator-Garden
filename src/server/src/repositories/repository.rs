use axum::async_trait;

use crate::errors::error::DbResult;

#[async_trait]
pub trait Repository<T, Id> {
    async fn create(&self, object: T) -> DbResult<T>;
    async fn find_by_id(&self, id: Id) -> DbResult<Option<T>>;
    async fn find_all(&self) -> DbResult<Vec<T>>;
    async fn update(&self, id: Id, object: T) -> DbResult<T>;
    async fn delete(&self, id: Id) -> DbResult<bool>;
}
