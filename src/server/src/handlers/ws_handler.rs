use axum::{
    extract::{
        State, WebSocketUpgrade,
        ws::{Message, WebSocket},
    },
    response::IntoResponse,
};

use crate::state::app_state::AppState;

async fn handle_socket(mut socket: WebSocket, app_state: AppState) {
    while let Some(Ok(msg)) = socket.recv().await {
        match msg {
            Message::Text(text) => {
                println!("Received: {}", text);
                // TODO хендлити
                if socket.send(Message::Text(text)).await.is_err() {
                    break; // Connection close!
                }
            }
            Message::Binary(_) => println!("Received binary data"),
            Message::Close(_) => break,
            _ => (),
        }
    }
}

pub async fn handler(ws: WebSocketUpgrade, State(app_state): State<AppState>) -> impl IntoResponse {
    ws.on_upgrade(|socket| handle_socket(socket, app_state))
}
