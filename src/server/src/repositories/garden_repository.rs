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
        todo!()
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Garden>> {
        todo!()
    }

    async fn find_all(&self) -> DbResult<Vec<Garden>> {
        todo!()
    }

    async fn update(&self, id: i32, object: Garden) -> DbResult<Garden> {
        todo!()
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        todo!()
    }
}

#[async_trait]
impl GardenRepository for PgGardenRepository {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<Garden>> {
        todo!()
    }

    async fn find_by_name(&self, name: &str) -> DbResult<Vec<Garden>> {
        todo!()
    }

    async fn find_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<Option<Garden>> {
        todo!()
    }

    async fn exists_by_user_id_and_name(&self, user_id: i32, name: &str) -> DbResult<bool> {
        todo!()
    }
}
