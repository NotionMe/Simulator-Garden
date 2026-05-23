use async_trait::async_trait;
use sqlx::PgPool;

use crate::{
    errors::error::DbResult,
    models::PlayerInventoryItem,
    repositories::repository::Repository,
};

#[async_trait]
pub trait PlayerInventoryItemRepository: Repository<PlayerInventoryItem, i32> {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<PlayerInventoryItem>>;
    async fn find_by_plant_id(&self, plant_id: i32) -> DbResult<Vec<PlayerInventoryItem>>;
    async fn find_by_user_id_and_plant_id(
        &self,
        user_id: i32,
        plant_id: i32,
    ) -> DbResult<Option<PlayerInventoryItem>>;
}

pub struct PgPlayerInventoryItemRepository {
    pool: PgPool,
}

impl PgPlayerInventoryItemRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<PlayerInventoryItem, i32> for PgPlayerInventoryItemRepository {
    async fn create(&self, object: PlayerInventoryItem) -> DbResult<PlayerInventoryItem> {
        let sql = r#"
            INSERT INTO player_inventory_items (user_id, plant_id, quantity, update_at)
            VALUES ($1, $2, $3, $4)
            RETURNING *"#;

        let inserted = sqlx::query_as::<_, PlayerInventoryItem>(sql)
            .bind(object.user_id)
            .bind(object.plant_id)
            .bind(object.quantity)
            .bind(object.update_at)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<PlayerInventoryItem>> {
        let sql = "SELECT * FROM player_inventory_items WHERE id = $1";

        let found = sqlx::query_as::<_, PlayerInventoryItem>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_all(&self) -> DbResult<Vec<PlayerInventoryItem>> {
        let sql = "SELECT * FROM player_inventory_items";

        let found = sqlx::query_as::<_, PlayerInventoryItem>(sql)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn update(&self, id: i32, object: PlayerInventoryItem) -> DbResult<PlayerInventoryItem> {
        let sql = r#"
            UPDATE player_inventory_items
            SET
                user_id = $1,
                plant_id = $2,
                quantity = $3,
                update_at = $4
            WHERE id = $5
            RETURNING *"#;

        let updated = sqlx::query_as::<_, PlayerInventoryItem>(sql)
            .bind(object.user_id)
            .bind(object.plant_id)
            .bind(object.quantity)
            .bind(object.update_at)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(updated)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM player_inventory_items WHERE id = $1";

        let deleted = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        Ok(deleted.rows_affected() == 1)
    }
}

#[async_trait]
impl PlayerInventoryItemRepository for PgPlayerInventoryItemRepository {
    async fn find_by_user_id(&self, user_id: i32) -> DbResult<Vec<PlayerInventoryItem>> {
        let sql = "SELECT * FROM player_inventory_items WHERE user_id = $1";

        let found = sqlx::query_as::<_, PlayerInventoryItem>(sql)
            .bind(user_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_plant_id(&self, plant_id: i32) -> DbResult<Vec<PlayerInventoryItem>> {
        let sql = "SELECT * FROM player_inventory_items WHERE plant_id = $1";

        let found = sqlx::query_as::<_, PlayerInventoryItem>(sql)
            .bind(plant_id)
            .fetch_all(&self.pool)
            .await?;

        Ok(found)
    }

    async fn find_by_user_id_and_plant_id(
        &self,
        user_id: i32,
        plant_id: i32,
    ) -> DbResult<Option<PlayerInventoryItem>> {
        let sql = "SELECT * FROM player_inventory_items WHERE user_id = $1 AND plant_id = $2";

        let found = sqlx::query_as::<_, PlayerInventoryItem>(sql)
            .bind(user_id)
            .bind(plant_id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(found)
    }
}
