use axum::{
    extract::{
        ws::{Message, WebSocket},
        WebSocketUpgrade,
    },
    response::IntoResponse,
};

async fn handle_socket(mut socket: WebSocket) {
    while let Some(Ok(msg)) = socket.recv().await {
        match msg {
            Message::Text(text) => {
                println!("Received: {}", text);
                // TODO хендлити
                if socket.send(Message::Text(text)).await.is_err() {
                    break; // Connection lost
                }
            }
            Message::Binary(_) => println!("Received binary data"),
            Message::Close(_) => break,
            _ => (),
        }
    }
}

async fn handler(ws: WebSocketUpgrade) -> impl IntoResponse {
    ws.on_upgrade(handle_socket)
}
