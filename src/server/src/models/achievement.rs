use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct Achievement {
    pub id: i32,
    pub user_id: i32,
    pub title: String,
    pub condition_key: String,
    pub earned_at: DateTime<Utc>,
}
