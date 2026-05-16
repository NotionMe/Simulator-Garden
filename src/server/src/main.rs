mod config;
mod errors;
mod handlers;
mod models;
mod repositories;
mod state;

use state::app_state::AppState;
use tracing::info;

use crate::repositories::{
    repository::Repository,
    user_repository::{PgUserRepository, UserRepository},
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt().init();

    // Initialize application state
    let app_state = AppState::new().await?;

    let user_repo = PgUserRepository::new(app_state.db_pool);

    let delete = user_repo.delete(4).await?;
    Ok(())
}
