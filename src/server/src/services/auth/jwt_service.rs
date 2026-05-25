use chrono::Utc;
use jsonwebtoken::{DecodingKey, EncodingKey, Header, Validation, encode, decode};
use serde::{Deserialize, Serialize};

use crate::{
    dto::UserResponseDto,
    errors::error::{AppError, AppResult},
};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct JwtClaims {
    pub sub: String,
    pub username: String,
    pub email: String,
    pub exp: usize,
    pub iat: usize,
}

#[derive(Debug, Clone)]
pub struct JwtService {
    encoding_key: EncodingKey,
    decoding_key: DecodingKey,
    expiration_hours: i64,
}

impl JwtService {
    pub fn new(secret: &str, expiration_hours: i64) -> Self {
        Self {
            encoding_key: EncodingKey::from_secret(secret.as_bytes()),
            decoding_key: DecodingKey::from_secret(secret.as_bytes()),
            expiration_hours,
        }
    }

    pub fn issue_token(&self, user: &UserResponseDto) -> AppResult<String> {
        let now = Utc::now().timestamp();
        let exp = now + self.expiration_hours * 3600;

        let claims = JwtClaims {
            sub: user.id.to_string(),
            username: user.username.clone(),
            email: user.email.clone(),
            exp: exp as usize,
            iat: now as usize,
        };

        encode(&Header::default(), &claims, &self.encoding_key).map_err(|_| AppError::TokenError)
    }

    pub fn validate_token(&self, token: &str) -> AppResult<JwtClaims> {
        let token_data = decode::<JwtClaims>(token, &self.decoding_key, &Validation::default())
            .map_err(|_| AppError::InvalidToken)?;

        Ok(token_data.claims)
    }
}
