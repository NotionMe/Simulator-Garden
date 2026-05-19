use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use crate::models::{Task, TaskType};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreateTaskDto {
    pub plant_instance_id: i32,
    pub task_type: TaskType,
    pub due_at: DateTime<Utc>,
    pub is_done: Option<bool>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdateTaskDto {
    pub plant_instance_id: Option<i32>,
    pub task_type: Option<TaskType>,
    pub due_at: Option<DateTime<Utc>>,
    pub is_done: Option<bool>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TaskResponseDto {
    pub id: i32,
    pub plant_instance_id: i32,
    pub task_type: TaskType,
    pub due_at: DateTime<Utc>,
    pub is_done: bool,
}

impl From<Task> for TaskResponseDto {
    fn from(task: Task) -> Self {
        Self {
            id: task.id,
            plant_instance_id: task.plant_instance_id,
            task_type: task.task_type,
            due_at: task.due_at,
            is_done: task.is_done,
        }
    }
}
