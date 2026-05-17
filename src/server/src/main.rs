use axum::{Router, routing::get};
use server::{handlers::ws_handler, state::app_state::AppState};
use tokio::net::TcpListener;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt().init();

    let app_state = AppState::new().await?;

    let app = Router::new()
        .route("/server/ws", get(ws_handler::handler))
        .with_state(app_state);

    let listener = TcpListener::bind("127.0.0.1:3000").await?;
    axum::serve(listener, app).await?;
    Ok(())
}
