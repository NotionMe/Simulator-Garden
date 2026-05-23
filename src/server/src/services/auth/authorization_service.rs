use argon2::password_hash::rand_core::OsRng;
use argon2::password_hash::{PasswordHash, SaltString};
use argon2::{Argon2, PasswordHasher, PasswordVerifier};

use crate::errors::error::{AppError, AppResult, DatabaseError, DbResult};

#[derive(Debug, Clone, Copy, Default)]
pub struct AuthorizationService;

impl AuthorizationService {
    pub fn new() -> Self {
        Self
    }

    pub fn hash_password(&self, raw_password: &str) -> AppResult<String> {
        if raw_password.trim().is_empty() {
            return Err(AppError::PasswordEmpty);
        }

        let salt = SaltString::generate(&mut OsRng);
        Argon2::default()
            .hash_password(raw_password.as_bytes(), &salt)
            .map(|hash| hash.to_string())
            .map_err(|_| AppError::AuthError)
    }

    pub fn verify_password(&self, raw_password: &str, password_hash: &str) -> AppResult<bool> {
        if raw_password.trim().is_empty() {
            return Err(AppError::PasswordEmpty);
        }

        let parsed_hash = PasswordHash::new(password_hash).map_err(|_| AppError::AuthError)?;

        Ok(Argon2::default()
            .verify_password(raw_password.as_bytes(), &parsed_hash)
            .is_ok())
    }

    pub fn authenticate_login(&self, password: &str, stored_hash: &str) -> DbResult<()> {
        let is_valid = self
            .verify_password(password, stored_hash)
            .map_err(|_| DatabaseError::InvalidCredentials)?;

        if !is_valid {
            return Err(DatabaseError::InvalidCredentials);
        }

        Ok(())
    }
}
