use chrono::Utc;

use crate::{
    dto::{CreateTaskDto, TaskResponseDto, UpdateTaskDto},
    errors::error::{DatabaseError, DbResult},
    models::Task,
    repositories::task_repository::TaskRepository,
};

pub struct TaskService<R>
where
    R: TaskRepository + Sync,
{
    repo: R,
}

impl<R> TaskService<R>
where
    R: TaskRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_task(&self, dto: CreateTaskDto) -> DbResult<TaskResponseDto> {
        let task = Task {
            id: 0,
            plant_instance_id: dto.plant_instance_id,
            task_type: dto.task_type,
            due_at: dto.due_at,
            is_done: dto.is_done.unwrap_or(false),
        };

        let created = self.repo.create(task).await?;

        Ok(TaskResponseDto::from(created))
    }

    pub async fn get_task(&self, id: i32) -> DbResult<TaskResponseDto> {
        let task = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(TaskResponseDto::from(task))
    }

    pub async fn get_tasks_by_plant_instance(
        &self,
        plant_instance_id: i32,
    ) -> DbResult<Vec<TaskResponseDto>> {
        let tasks = self
            .repo
            .find_by_plant_instance_id(plant_instance_id)
            .await?;

        Ok(tasks.into_iter().map(TaskResponseDto::from).collect())
    }

    pub async fn get_pending_tasks_by_plant_instance(
        &self,
        plant_instance_id: i32,
    ) -> DbResult<Vec<TaskResponseDto>> {
        let tasks = self
            .repo
            .find_pending_by_plant_instance_id(plant_instance_id)
            .await?;

        Ok(tasks.into_iter().map(TaskResponseDto::from).collect())
    }

    pub async fn get_overdue_tasks(&self) -> DbResult<Vec<TaskResponseDto>> {
        let now = Utc::now();
        let tasks = self.repo.find_overdue(now).await?;

        Ok(tasks.into_iter().map(TaskResponseDto::from).collect())
    }

    pub async fn get_all_tasks(&self) -> DbResult<Vec<TaskResponseDto>> {
        let tasks = self.repo.find_all().await?;

        Ok(tasks.into_iter().map(TaskResponseDto::from).collect())
    }

    pub async fn mark_task_done(&self, id: i32) -> DbResult<TaskResponseDto> {
        let task = self
            .repo
            .mark_done(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(TaskResponseDto::from(task))
    }

    pub async fn update_task(&self, id: i32, dto: UpdateTaskDto) -> DbResult<TaskResponseDto> {
        let mut task = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(plant_instance_id) = dto.plant_instance_id {
            task.plant_instance_id = plant_instance_id;
        }

        if let Some(task_type) = dto.task_type {
            task.task_type = task_type;
        }

        if let Some(due_at) = dto.due_at {
            task.due_at = due_at;
        }

        if let Some(is_done) = dto.is_done {
            task.is_done = is_done;
        }

        let updated = self.repo.update(id, task).await?;

        Ok(TaskResponseDto::from(updated))
    }

    pub async fn delete_task(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
