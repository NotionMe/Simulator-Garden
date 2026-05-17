use server::{
    repositories::user_repository::{PgUserRepository, UserRepository},
    state::app_state::AppState,
};
use tracing::info;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt().init();

    // Initialize application state
    let app_state = AppState::new().await?;

    let user_repo = PgUserRepository::new(app_state.db_pool);

    let exist_by_usr = user_repo.exists_by_username("john_doe").await?;
    info!("find usr: {}", exist_by_usr);

    let exist_by_email = user_repo.exists_by_email("john@example.com").await?;
    info!("find usr: {}", exist_by_email);

    Ok(())
}
