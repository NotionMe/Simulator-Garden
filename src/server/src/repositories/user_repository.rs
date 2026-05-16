use async_trait::async_trait;
use sqlx::PgPool;
use tracing::info;

use crate::{errors::error::DbResult, models::User, repositories::repository::Repository};

#[async_trait]
pub trait UserRepository: Repository<User, i32> {
    async fn find_by_username(&self, username: &str) -> DbResult<Option<User>>;
    async fn find_by_email(&self, email: &str) -> DbResult<Option<User>>;

    async fn exists_by_username(&self, username: &str) -> DbResult<bool>;
    async fn exists_by_email(&self, email: &str) -> DbResult<bool>;
}

pub struct PgUserRepository {
    pool: PgPool,
}

impl PgUserRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository<User, i32> for PgUserRepository {
    async fn create(&self, object: User) -> DbResult<User> {
        let sql = "INSERT INTO users (username, email, password_hash, created_at) VALUES ($1,$2,$3,$4) RETURNING *";

        let inserted = sqlx::query_as::<_, User>(sql)
            .bind(object.username)
            .bind(object.email)
            .bind(object.password_hash)
            .bind(object.created_at)
            .fetch_one(&self.pool)
            .await?;

        Ok(inserted)
    }

    async fn find_all(&self) -> DbResult<Vec<User>> {
        let sql = "SELECT * FROM users";

        let find = sqlx::query_as::<_, User>(sql).fetch_all(&self.pool).await?;

        Ok(find)
    }

    async fn find_by_id(&self, id: i32) -> DbResult<Option<User>> {
        let sql = "SELECT id FROM users WHERE id = $1";

        let find = sqlx::query_as::<_, User>(sql)
            .bind(id)
            .fetch_optional(&self.pool)
            .await?;

        Ok(find)
    }

    async fn update(&self, id: i32, object: User) -> DbResult<User> {
        let sql = r#"
            UPDATE users
                SET
                    username =  COALESCE($1, username),
                    email =  COALESCE($2, email),
                    password_hash =  COALESCE($3, password_hash),
                    created_at = COALESCE($4, created_at)
            WHERE id = $5"#;

        let update = sqlx::query_as::<_, User>(sql)
            .bind(object.username)
            .bind(object.email)
            .bind(object.password_hash)
            .bind(object.created_at)
            .bind(id)
            .fetch_one(&self.pool)
            .await?;

        Ok(update)
    }

    async fn delete(&self, id: i32) -> DbResult<bool> {
        let sql = "DELETE FROM users WHERE id = $1";

        let delete = sqlx::query(sql).bind(id).execute(&self.pool).await?;

        if delete.rows_affected() == 1 {
            return Ok(true);
        } else {
            return Ok(false);
        }
    }
}

#[async_trait]
impl UserRepository for PgUserRepository {
    async fn find_by_username(&self, username: &str) -> DbResult<Option<User>> {
        todo!()
    }

    async fn find_by_email(&self, email: &str) -> DbResult<Option<User>> {
        todo!()
    }

    async fn exists_by_username(&self, username: &str) -> DbResult<bool> {
        todo!()
    }

    async fn exists_by_email(&self, email: &str) -> DbResult<bool> {
        todo!()
    }
}
