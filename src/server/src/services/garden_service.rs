use chrono::Utc;

use crate::{
    dto::{CreateGardenDto, GardenResponseDto, UpdateGardenDto},
    errors::error::{DatabaseError, DbResult},
    models::Garden,
    repositories::garden_repository::GardenRepository,
};

pub struct GardenService<R>
where
    R: GardenRepository + Sync,
{
    repo: R,
}

impl<R> GardenService<R>
where
    R: GardenRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_garden(&self, dto: CreateGardenDto) -> DbResult<GardenResponseDto> {
        if dto.name.trim().is_empty() {
            return Err(DatabaseError::FailedToSave);
        }

        if dto.width_cells <= 0 || dto.height_cells <= 0 {
            return Err(DatabaseError::FailedToSave);
        }

        if self
            .repo
            .exists_by_user_id_and_name(dto.user_id, &dto.name)
            .await?
        {
            return Err(DatabaseError::FailedToSave);
        }

        let garden = Garden {
            id: 0,
            user_id: dto.user_id,
            name: dto.name,
            width_cells: dto.width_cells,
            height_cells: dto.height_cells,
            created_at: Utc::now(),
        };

        let created = self.repo.create(garden).await?;

        Ok(GardenResponseDto::from(created))
    }

    pub async fn get_garden(&self, id: i32) -> DbResult<GardenResponseDto> {
        let garden = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(GardenResponseDto::from(garden))
    }

    pub async fn get_gardens_by_user(&self, user_id: i32) -> DbResult<Vec<GardenResponseDto>> {
        let gardens = self.repo.find_by_user_id(user_id).await?;

        Ok(gardens.into_iter().map(GardenResponseDto::from).collect())
    }

    pub async fn get_all_gardens(&self) -> DbResult<Vec<GardenResponseDto>> {
        let gardens = self.repo.find_all().await?;

        Ok(gardens.into_iter().map(GardenResponseDto::from).collect())
    }

    pub async fn update_garden(
        &self,
        id: i32,
        dto: UpdateGardenDto,
    ) -> DbResult<GardenResponseDto> {
        let mut garden = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(user_id) = dto.user_id {
            garden.user_id = user_id;
        }

        if let Some(name) = dto.name {
            if name.trim().is_empty() {
                return Err(DatabaseError::FailedToUpdate);
            }
            garden.name = name;
        }

        if let Some(width_cells) = dto.width_cells {
            if width_cells <= 0 {
                return Err(DatabaseError::FailedToUpdate);
            }
            garden.width_cells = width_cells;
        }

        if let Some(height_cells) = dto.height_cells {
            if height_cells <= 0 {
                return Err(DatabaseError::FailedToUpdate);
            }
            garden.height_cells = height_cells;
        }

        let updated = self.repo.update(id, garden).await?;

        Ok(GardenResponseDto::from(updated))
    }

    pub async fn delete_garden(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
