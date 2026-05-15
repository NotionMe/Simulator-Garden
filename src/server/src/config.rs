use std::env;

use dotenv::dotenv;

pub struct DatabaseConfig {
    pub url: String,
    pub pool_size: u32,
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
