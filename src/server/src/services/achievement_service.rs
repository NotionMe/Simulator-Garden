use chrono::Utc;

use crate::{
    dto::{AchievementResponseDto, CreateAchievementDto, UpdateAchievementDto},
    errors::error::{DatabaseError, DbResult},
    models::Achievement,
    repositories::achievement_repository::AchievementRepository,
};

pub struct AchievementService<R>
where
    R: AchievementRepository + Sync,
{
    repo: R,
}

impl<R> AchievementService<R>
where
    R: AchievementRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_achievement(
        &self,
        dto: CreateAchievementDto,
    ) -> DbResult<AchievementResponseDto> {
        if dto.title.trim().is_empty() {
            return Err(DatabaseError::FailedToSave);
        }

        if dto.condition_key.trim().is_empty() {
            return Err(DatabaseError::FailedToSave);
        }

        if self
            .repo
            .exists_by_user_id_and_condition_key(dto.user_id, &dto.condition_key)
            .await?
        {
            return Err(DatabaseError::FailedToSave);
        }

        let achievement = Achievement {
            id: 0,
            user_id: dto.user_id,
            title: dto.title,
            condition_key: dto.condition_key,
            earned_at: Utc::now(),
        };

        let created = self.repo.create(achievement).await?;

        Ok(AchievementResponseDto::from(created))
    }

    pub async fn get_achievement(&self, id: i32) -> DbResult<AchievementResponseDto> {
        let achievement = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(AchievementResponseDto::from(achievement))
    }

    pub async fn get_achievements_by_user(
        &self,
        user_id: i32,
    ) -> DbResult<Vec<AchievementResponseDto>> {
        let achievements = self.repo.find_by_user_id(user_id).await?;

        Ok(achievements
            .into_iter()
            .map(AchievementResponseDto::from)
            .collect())
    }

    pub async fn get_all_achievements(&self) -> DbResult<Vec<AchievementResponseDto>> {
        let achievements = self.repo.find_all().await?;

        Ok(achievements
            .into_iter()
            .map(AchievementResponseDto::from)
            .collect())
    }

    pub async fn update_achievement(
        &self,
        id: i32,
        dto: UpdateAchievementDto,
    ) -> DbResult<AchievementResponseDto> {
        let mut achievement = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(user_id) = dto.user_id {
            achievement.user_id = user_id;
        }

        if let Some(title) = dto.title {
            if title.trim().is_empty() {
                return Err(DatabaseError::FailedToUpdate);
            }
            achievement.title = title;
        }

        if let Some(condition_key) = dto.condition_key {
            if condition_key.trim().is_empty() {
                return Err(DatabaseError::FailedToUpdate);
            }
            achievement.condition_key = condition_key;
        }

        if let Some(earned_at) = dto.earned_at {
            achievement.earned_at = earned_at;
        }

        let updated = self.repo.update(id, achievement).await?;

        Ok(AchievementResponseDto::from(updated))
    }

    pub async fn delete_achievement(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
