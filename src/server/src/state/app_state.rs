use std::{collections::HashMap, sync::Arc};

use axum::extract::ws::WebSocket;
use sqlx::{PgPool, postgres::PgPoolOptions};
use tokio::sync::Mutex;
use tracing::warn;

use crate::{
    config::{AuthConfig, DatabaseConfig},
    errors::error::DbResult,
    services::auth::{jwt_service::JwtService, signing_key_pair::SigningKeyPair},
};

#[derive(Debug, Clone)]
pub struct AppState {
    pub db_pool: PgPool,
    pub jwt: Arc<JwtService>,
    pub signing_keys: Arc<SigningKeyPair>,
    pub active_connections: Arc<Mutex<HashMap<String, WebSocket>>>,
}

impl AppState {
    pub async fn new() -> DbResult<Self> {
        let config_db = DatabaseConfig::config().await;
        let auth_config = AuthConfig::load();

        let pool = PgPoolOptions::new()
            .max_connections(config_db.pool_size)
            .connect(&config_db.url)
            .await?;

        let jwt = Arc::new(JwtService::new(
            &auth_config.jwt_secret,
            auth_config.jwt_expiration_hours,
        ));

        let signing_keys = match auth_config.ed25519_signing_key_hex.as_deref() {
            Some(hex) => Arc::new(SigningKeyPair::from_secret_hex(hex).map_err(|_| {
                sqlx::Error::Configuration("Invalid ED25519_SIGNING_KEY_HEX".into())
            })?),
            None => {
                warn!(
                    "ED25519_SIGNING_KEY_HEX is not set; generating ephemeral signing keys for this process"
                );
                Arc::new(SigningKeyPair::generate())
            }
        };

        Ok(Self {
            db_pool: pool,
            jwt,
            signing_keys,
            active_connections: Arc::new(Mutex::new(HashMap::new())),
        })
    }
}
