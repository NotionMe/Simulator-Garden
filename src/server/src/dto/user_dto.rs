use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

use crate::models::User;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CreateUserDto {
    pub username: String,
    pub email: String,
    pub password: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UpdateUserDto {
    pub username: Option<String>,
    pub email: Option<String>,
    pub password: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct LoginUserDto {
    pub username: Option<String>,
    pub email: Option<String>,
    pub password: String,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum LoginIdentifier<'a> {
    Username(&'a str),
    Email(&'a str),
}

impl LoginUserDto {
    pub fn identifier(&self) -> Option<LoginIdentifier<'_>> {
        match (
            self.username
                .as_deref()
                .filter(|username| !username.trim().is_empty()),
            self.email
                .as_deref()
                .filter(|email| !email.trim().is_empty()),
        ) {
            (Some(username), None) => Some(LoginIdentifier::Username(username)),
            (None, Some(email)) => Some(LoginIdentifier::Email(email)),
            _ => None,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserResponseDto {
    pub id: i32,
    pub username: String,
    pub email: String,
    pub created_at: NaiveDateTime,
}

impl From<User> for UserResponseDto {
    fn from(user: User) -> Self {
        Self {
            id: user.id,
            username: user.username,
            email: user.email,
            created_at: user.created_at,
        }
    }
}
