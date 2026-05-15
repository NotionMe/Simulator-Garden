use std::{collections::HashMap, sync::Arc};

use axum::extract::ws::WebSocket;
use sqlx::{PgPool, postgres::PgPoolOptions};
use tokio::sync::Mutex;

use crate::{config::DatabaseConfig, errors::error::DbResult};

#[derive(Debug, Clone)]
pub struct AppState {
    pub db_pool: PgPool,
    pub active_connections: Arc<Mutex<HashMap<String, WebSocket>>>,
}

impl AppState {
    pub async fn new() -> DbResult<Self> {
        let config_db = DatabaseConfig::config().await;

        let pool = PgPoolOptions::new()
            .max_connections(config_db.pool_size)
            .connect(&config_db.url)
            .await?;

        Ok(Self {
            db_pool: pool,
            active_connections: Arc::new(Mutex::new(HashMap::new())),
        })
    }
}
