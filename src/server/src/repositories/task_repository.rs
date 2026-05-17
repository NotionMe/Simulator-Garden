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
        let sql = "INSERT INTO tasks (plant_instance_id, task_type, due_at, is_done) VALUES ($1,$2,$3,$4) RETURNING *";

        let inserted = sqlx::query_as::<_, Task>(sql)
            .bind(object.plant_instance_id)
            .bind(object.task_type)
            .bind(object.due_at)
            .bind(object.is_done)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_all(&self) -> DbResult<Vec<Task>> {
        let sql = "SELECT * FROM tasks";

        let find = sqlx::query_as::<_, Task>(sql).fetch_all(&self.pool).await?;

        Ok(find)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<Task>> {
        let sql = "SELECT * FROM tasks WHERE id = $1";

        let found = sqlx::query_as::<_, Task>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn update(&self, id: i32, object: Task) -> DbResult<Task> {
        let sql = r#"
            UPDATE tasks
            SET
                plant_instance_id = $1,
                task_type = $2,
                due_at = $3,
                is_done = $4
            WHERE id = $5
            RETURNING *"#;

        let updated = sqlx::query_as::<_, Task>(sql)
            .bind(object.plant_instance_id)
            .bind(object.task_type)
            .bind(object.due_at)
            .bind(object.is_done)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(updated)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM tasks WHERE id = $1";

        let deleted = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        Ok(deleted.rows_affected() == 1)
    }
}

#[async_trait]
impl TaskRepository for PgTaskRepository {
    async fn find_by_plant_instance_id(&self, plant_instance_id: i32) -> DbResult<Vec<Task>> {
        let sql = "SELECT * FROM tasks WHERE plant_instance_id = $1";

        let found = sqlx::query_as::<_, Task>(sql)
            .bind(plant_instance_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_task_type(&self, task_type: TaskType) -> DbResult<Vec<Task>> {
        let sql = "SELECT * FROM tasks WHERE task_type = $1";

        let found = sqlx::query_as::<_, Task>(sql)
            .bind(task_type)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_pending_by_plant_instance_id(
        &self,
        plant_instance_id: i32,
    ) -> DbResult<Vec<Task>> {
        let sql = "SELECT * FROM tasks WHERE plant_instance_id = $1 AND is_done = FALSE";

        let found = sqlx::query_as::<_, Task>(sql)
            .bind(plant_instance_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_overdue(&self, now: DateTime<Utc>) -> DbResult<Vec<Task>> {
        let sql = "SELECT * FROM tasks WHERE due_at < $1 AND is_done = FALSE";

        let found = sqlx::query_as::<_, Task>(sql)
            .bind(now)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn mark_done(&self, id: i32) -> DbResult<Option<Task>> {
        let sql = r#"
            UPDATE tasks
            SET is_done = TRUE
            WHERE id = $1
            RETURNING *"#;

        let updated = sqlx::query_as::<_, Task>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(updated)
    }
}
