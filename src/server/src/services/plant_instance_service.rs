use chrono::Utc;

use crate::{
    dto::{CreatePlantInstanceDto, PlantInstanceResponseDto, UpdatePlantInstanceDto},
    errors::error::{DatabaseError, DbResult},
    models::PlantInstance,
    repositories::plant_instance_repository::PlantInstanceRepository,
};

pub struct PlantInstanceService<R>
where
    R: PlantInstanceRepository + Sync,
{
    repo: R,
}

impl<R> PlantInstanceService<R>
where
    R: PlantInstanceRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_plant_instance(
        &self,
        dto: CreatePlantInstanceDto,
    ) -> DbResult<PlantInstanceResponseDto> {
        if self
            .repo
            .exists_at_position(dto.garden_id, dto.cell_x, dto.cell_y)
            .await?
        {
            return Err(DatabaseError::FailedToSave);
        }

        let plant_instance = PlantInstance {
            id: 0,
            garden_id: dto.garden_id,
            plant_id: dto.plant_id,
            cell_x: dto.cell_x,
            cell_y: dto.cell_y,
            planted_at: Utc::now().naive_utc(),
            growth_stage: dto.growth_stage.unwrap_or(0),
            is_watered: dto.is_watered.unwrap_or(false),
            is_fertilized: dto.is_fertilized.unwrap_or(false),
        };

        let created = self.repo.create(plant_instance).await?;

        Ok(PlantInstanceResponseDto::from(created))
    }

    pub async fn get_plant_instance(&self, id: i32) -> DbResult<PlantInstanceResponseDto> {
        let plant_instance = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(PlantInstanceResponseDto::from(plant_instance))
    }

    pub async fn get_plant_instances_by_garden(
        &self,
        garden_id: i32,
    ) -> DbResult<Vec<PlantInstanceResponseDto>> {
        let plant_instances = self.repo.find_by_garden_id(garden_id).await?;

        Ok(plant_instances
            .into_iter()
            .map(PlantInstanceResponseDto::from)
            .collect())
    }

    pub async fn get_all_plant_instances(&self) -> DbResult<Vec<PlantInstanceResponseDto>> {
        let plant_instances = self.repo.find_all().await?;

        Ok(plant_instances
            .into_iter()
            .map(PlantInstanceResponseDto::from)
            .collect())
    }

    pub async fn update_plant_instance(
        &self,
        id: i32,
        dto: UpdatePlantInstanceDto,
    ) -> DbResult<PlantInstanceResponseDto> {
        let mut plant_instance = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(garden_id) = dto.garden_id {
            plant_instance.garden_id = garden_id;
        }

        if let Some(plant_id) = dto.plant_id {
            plant_instance.plant_id = plant_id;
        }

        if let Some(cell_x) = dto.cell_x {
            plant_instance.cell_x = cell_x;
        }

        if let Some(cell_y) = dto.cell_y {
            plant_instance.cell_y = cell_y;
        }

        if let Some(planted_at) = dto.planted_at {
            plant_instance.planted_at = planted_at;
        }

        if let Some(growth_stage) = dto.growth_stage {
            plant_instance.growth_stage = growth_stage;
        }

        if let Some(is_watered) = dto.is_watered {
            plant_instance.is_watered = is_watered;
        }

        if let Some(is_fertilized) = dto.is_fertilized {
            plant_instance.is_fertilized = is_fertilized;
        }

        let updated = self.repo.update(id, plant_instance).await?;

        Ok(PlantInstanceResponseDto::from(updated))
    }

    pub async fn delete_plant_instance(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
