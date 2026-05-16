use axum::async_trait;

use chrono::{DateTime, Utc};

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
