use chrono::Utc;

use crate::{
    dto::{CreateUserDto, LoginIdentifier, LoginUserDto, UpdateUserDto, UserResponseDto},
    errors::error::{DatabaseError, DbResult},
    models::User,
    repositories::user_repository::UserRepository,
    services::auth::authorization_service::AuthorizationService,
};

pub struct UserService<R>
where
    R: UserRepository + Sync,
{
    repo: R,
}

impl<R> UserService<R>
where
    R: UserRepository + Sync,
{
    pub fn new(repo: R) -> Self {
        Self { repo }
    }

    pub async fn create_user(&self, dto: CreateUserDto) -> DbResult<UserResponseDto> {
        if dto.username.trim().is_empty() || dto.email.trim().is_empty() {
            return Err(DatabaseError::FailedToSave);
        }

        if dto.password.len() < 8 {
            return Err(DatabaseError::WeakPassword);
        }

        if self.repo.exists_by_username(&dto.username).await? {
            return Err(DatabaseError::UsernameTaken);
        }

        if self.repo.exists_by_email(&dto.email).await? {
            return Err(DatabaseError::EmailTaken);
        }

        let auth_service = AuthorizationService::new();
        let password_hash = auth_service
            .hash_password(&dto.password)
            .map_err(|_| DatabaseError::FailedToSave)?;

        let user = User {
            id: 0,
            username: dto.username,
            email: dto.email,
            password_hash,
            created_at: Utc::now().naive_utc(),
        };

        let created = self.repo.create(user).await?;

        Ok(UserResponseDto::from(created))
    }

    pub async fn login_user(&self, dto: LoginUserDto) -> DbResult<UserResponseDto> {
        let user = match dto.identifier().ok_or(DatabaseError::FailedToRead)? {
            LoginIdentifier::Username(username) => self.repo.find_by_username(username).await?,
            LoginIdentifier::Email(email) => self.repo.find_by_email(email).await?,
        }
        .ok_or(DatabaseError::NotFound)?;

        AuthorizationService::new().authenticate_login(&dto.password, &user.password_hash)?;

        Ok(UserResponseDto::from(user))
    }

    pub async fn get_user(&self, id: i32) -> DbResult<UserResponseDto> {
        let user = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(UserResponseDto::from(user))
    }

    pub async fn get_user_by_username(&self, username: &str) -> DbResult<UserResponseDto> {
        let user = self
            .repo
            .find_by_username(username)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(UserResponseDto::from(user))
    }

    pub async fn get_user_by_email(&self, email: &str) -> DbResult<UserResponseDto> {
        let user = self
            .repo
            .find_by_email(email)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        Ok(UserResponseDto::from(user))
    }

    pub async fn get_all_users(&self) -> DbResult<Vec<UserResponseDto>> {
        let users = self.repo.find_all().await?;

        Ok(users.into_iter().map(UserResponseDto::from).collect())
    }

    pub async fn update_user(&self, id: i32, dto: UpdateUserDto) -> DbResult<UserResponseDto> {
        let mut user = self
            .repo
            .find_by_id(id)
            .await?
            .ok_or(DatabaseError::NotFound)?;

        if let Some(username) = dto.username {
            if username.trim().is_empty() {
                return Err(DatabaseError::FailedToUpdate);
            }
            user.username = username;
        }

        if let Some(email) = dto.email {
            if email.trim().is_empty() {
                return Err(DatabaseError::FailedToUpdate);
            }
            user.email = email;
        }

        if let Some(password) = dto.password {
            user.password_hash = AuthorizationService::new()
                .hash_password(&password)
                .map_err(|_| DatabaseError::FailedToUpdate)?;
        }

        let updated = self.repo.update(id, user).await?;

        Ok(UserResponseDto::from(updated))
    }

    pub async fn delete_user(&self, id: i32) -> DbResult<()> {
        let deleted = self.repo.delete(id).await?;

        if !deleted {
            return Err(DatabaseError::NotFound);
        }

        Ok(())
    }
}
