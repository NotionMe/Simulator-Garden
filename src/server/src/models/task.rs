use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

use super::enums::TaskType;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct Task {
    pub id: i32,
    pub plant_instance_id: i32,
    pub task_type: TaskType,
    pub due_at: DateTime<Utc>,
    pub is_done: bool,
}
