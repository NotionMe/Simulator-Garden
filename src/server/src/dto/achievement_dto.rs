use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use crate::models::Achievement;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreateAchievementDto {
    pub user_id: i32,
    pub title: String,
    pub condition_key: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdateAchievementDto {
    pub user_id: Option<i32>,
    pub title: Option<String>,
    pub condition_key: Option<String>,
    pub earned_at: Option<DateTime<Utc>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AchievementResponseDto {
    pub id: i32,
    pub user_id: i32,
    pub title: String,
    pub condition_key: String,
    pub earned_at: DateTime<Utc>,
}

impl From<Achievement> for AchievementResponseDto {
    fn from(achievement: Achievement) -> Self {
        Self {
            id: achievement.id,
            user_id: achievement.user_id,
            title: achievement.title,
            condition_key: achievement.condition_key,
            earned_at: achievement.earned_at,
        }
    }
}
