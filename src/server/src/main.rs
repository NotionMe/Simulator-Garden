use axum::{
    Router,
    routing::{get, post},
};
use server::{
    handlers::{auth_http, ws_handler},
    state::app_state::AppState,
};
use tokio::net::TcpListener;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt().init();

    let app_state = AppState::new().await?;

    let app = Router::new()
        .route("/server/ws", get(ws_handler::handler))
        .route("/api/login", post(auth_http::post_authorization))
        .route("/api/register", post(auth_http::post_registration))
        .with_state(app_state);

    let listener = TcpListener::bind("0.0.0.0:3000").await?;
    axum::serve(listener, app).await?;
    Ok(())
}
