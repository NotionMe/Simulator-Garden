mod config;
mod errors;
mod handlers;
mod models;
mod state;

use state::app_state::AppState;
use tracing::info;

use crate::models::User;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt().init();

    // Initialize application state
    let app_state = AppState::new().await?;
    let mut conn = app_state.db_pool.acquire().await?;

    let sql = "INSERT INTO users (username, email, password_hash, created_at) VALUES
    ('john_doe', 'john@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5NU8fGvVVqhDu', NOW() - INTERVAL '60 days'),
    ('maria_garcia', 'maria.garcia@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5NU8fGvVVqhDu', NOW() - INTERVAL '7 days'),
    ('alex_chen', 'alex.chen@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5NU8fGvVVqhDu', NOW() - INTERVAL '180 days'),
    ('sarah_johnson', 'sarah.j@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5NU8fGvVVqhDu', NOW() - INTERVAL '30 days'),
    ('tom_wilson', 'tom.wilson@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5NU8fGvVVqhDu', NOW() - INTERVAL '2 days');
";

    // sqlx::query(sql).execute(&mut *conn).await?;

    let query_user = sqlx::query_as::<_, User>("SELECT * FROM users;")
        .fetch_all(&mut *conn)
        .await?;

    info!("Users: {:?}", query_user.get(1));

    Ok(())
}
