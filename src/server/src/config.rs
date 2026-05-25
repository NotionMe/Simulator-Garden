use std::env;

use dotenv::dotenv;

pub struct DatabaseConfig {
    pub url: String,
    pub pool_size: u32,
}

pub struct AuthConfig {
    pub jwt_secret: String,
    pub jwt_expiration_hours: i64,
    pub ed25519_signing_key_hex: Option<String>,
}

impl DatabaseConfig {
    pub async fn config() -> Self {
        dotenv().ok();

        DatabaseConfig {
            url: env::var("DATABASE_URL").expect("DATABASE_URL must be set"),
            pool_size: env::var("DB_POOL_SIZE")
                .unwrap_or_else(|_| "5".to_string())
                .parse()
                .expect("DB_POOL_SIZE must be a valid number"),
        }
    }
}

impl AuthConfig {
    pub fn load() -> Self {
        dotenv().ok();

        Self {
            jwt_secret: env::var("JWT_SECRET").expect("JWT_SECRET must be set"),
            jwt_expiration_hours: env::var("JWT_EXPIRATION_HOURS")
                .unwrap_or_else(|_| "24".to_string())
                .parse()
                .expect("JWT_EXPIRATION_HOURS must be a valid number"),
            ed25519_signing_key_hex: env::var("ED25519_SIGNING_KEY_HEX").ok(),
        }
    }
}
