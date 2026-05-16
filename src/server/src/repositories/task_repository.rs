use async_trait::async_trait;
use chrono::{DateTime, Utc};
use sqlx::PgPool;

use crate::{
    errors::error::DbResult,
    models::{Task, TaskType},
    repositories::repository::Repository,
};

#[async_trait]
pub trait TaskRepository: Repository<Task, i32> {
    async fn find_by_plant_instance_id(&self, plant_instance_id: i32) -> DbResult<Vec<Task>>;
    async fn find_by_task_type(&self, task_type: TaskType) -> DbResult<Vec<Task>>;
    async fn find_pending_by_plant_instance_id(
        &self,
        plant_instance_id: i32,
    ) -> DbResult<Vec<Task>>;
    async fn find_overdue(&self, now: DateTime<Utc>) -> DbResult<Vec<Task>>;
    async fn mark_done(&self, id: i32) -> DbResult<Option<Task>>;
}

pub struct PgTaskRepository {
    pool: PgPool,
}

impl PgTaskRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<Task, i32> for PgTaskRepository {
    async fn create(&self, object: Task) -> DbResult<Task> {
        todo!()
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Task>> {
        todo!()
    }

    async fn find_all(&self) -> DbResult<Vec<Task>> {
        todo!()
    }

    async fn update(&self, id: i32, object: Task) -> DbResult<Task> {
        todo!()
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        todo!()
    }
}

#[async_trait]
impl TaskRepository for PgTaskRepository {
    async fn find_by_plant_instance_id(&self, plant_instance_id: i32) -> DbResult<Vec<Task>> {
        todo!()
    }

    async fn find_by_task_type(&self, task_type: TaskType) -> DbResult<Vec<Task>> {
        todo!()
    }

    async fn find_pending_by_plant_instance_id(
        &self,
        plant_instance_id: i32,
    ) -> DbResult<Vec<Task>> {
        todo!()
    }

    async fn find_overdue(&self, now: DateTime<Utc>) -> DbResult<Vec<Task>> {
        todo!()
    }

    async fn mark_done(&self, id: i32) -> DbResult<Option<Task>> {
        todo!()
    }
}
