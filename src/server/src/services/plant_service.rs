use crate::{
    dto::{CreatePlantDto, PlantResponseDto, UpdatePlantDto},
    errors::error::{DatabaseError, DbResult},
    models::Plant,
    repositories::plant_repository::PlantRepository,
};

pub struct PlantService<R>
where
    R: PlantRepository + Sync,
{
    repo: R,
}

impl<R> PlantService<R>
where
    R: PlantRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_plant(&self, dto: CreatePlantDto) -> DbResult<PlantResponseDto> {
        if dto.name.trim().is_empty() {
            return Err(DatabaseError::FailedToSave);
        }

        if dto.species.trim().is_empty() {
            return Err(DatabaseError::FailedToSave);
        }

        if dto.growth_days <= 0 {
            return Err(DatabaseError::FailedToSave);
        }

        let plant = Plant {
            id: 0,
            name: dto.name,
            species: dto.species,
            growth_days: dto.growth_days,
            climate_type: dto.climate_type,
            icon_key: dto.icon_key,
        };

        let created = self.repo.create(plant).await?;

        Ok(PlantResponseDto::from(created))
    }

    pub async fn get_plant(&self, id: i32) -> DbResult<PlantResponseDto> {
        let plant = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(PlantResponseDto::from(plant))
    }

    pub async fn get_all_plants(&self) -> DbResult<Vec<PlantResponseDto>> {
        let plants = self.repo.find_all().await?;

        Ok(plants.into_iter().map(PlantResponseDto::from).collect())
    }

    pub async fn search_plants_by_name(&self, query: &str) -> DbResult<Vec<PlantResponseDto>> {
        let plants = self.repo.search_by_name(query).await?;

        Ok(plants.into_iter().map(PlantResponseDto::from).collect())
    }

    pub async fn update_plant(&self, id: i32, dto: UpdatePlantDto) -> DbResult<PlantResponseDto> {
        let mut plant = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(name) = dto.name {
            if name.trim().is_empty() {
                return Err(DatabaseError::FailedToUpdate);
            }
            plant.name = name;
        }

        if let Some(species) = dto.species {
            if species.trim().is_empty() {
                return Err(DatabaseError::FailedToUpdate);
            }
            plant.species = species;
        }

        if let Some(growth_days) = dto.growth_days {
            if growth_days <= 0 {
                return Err(DatabaseError::FailedToUpdate);
            }
            plant.growth_days = growth_days;
        }

        if let Some(climate_type) = dto.climate_type {
            plant.climate_type = climate_type;
        }

        if let Some(icon_key) = dto.icon_key {
            plant.icon_key = icon_key;
        }

        let updated = self.repo.update(id, plant).await?;

        Ok(PlantResponseDto::from(updated))
    }

    pub async fn delete_plant(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
