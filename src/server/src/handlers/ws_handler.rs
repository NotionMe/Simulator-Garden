use axum::{
    extract::{
        State, WebSocketUpgrade,
        ws::{Message, WebSocket},
    },
    response::IntoResponse,
};

use crate::{
    handlers::{
        dispatcher::dispatch_request,
        messages::{RequestMessage, ResponseMessage, ResponseStatus},
    },
    state::app_state::AppState,
};

pub async fn handle_socket(mut socket: WebSocket, state: AppState) {
    while let Some(Ok(msg)) = socket.recv().await {
        if let Message::Text(text) = msg {
            // перевіряти чи запрос verified!
            
            let request = match serde_json::from_str::<RequestMessage>(&text) {
                Ok(request) => request,
                Err(err) => {
                    let response = ResponseMessage {
                        request_id: None,
                        status: ResponseStatus::Error,
                        command: crate::handlers::messages::CommandType::Read,
                        data: None,
                        error: Some(format!("Invalid request message: {}", err)),
                    };

                    let _ = socket
                        .send(Message::Text(
                            serde_json::to_string(&response).unwrap_or_default(),
                        ))
                        .await;

                    continue;
                }
            };

            let response = dispatch_request(request, state.clone()).await;

            let response_text = match serde_json::to_string(&response) {
                Ok(text) => text,
                Err(err) => {
                    let error_response = ResponseMessage {
                        request_id: response.request_id,
                        status: ResponseStatus::Error,
                        command: response.command,
                        data: None,
                        error: Some(format!("Failed to serialize response: {}", err)),
                    };
                    serde_json::to_string(&error_response).unwrap_or_default()
                }
            };

            let _ = socket.send(Message::Text(response_text)).await;
        }
    }
}

pub async fn handler(ws: WebSocketUpgrade, State(app_state): State<AppState>) -> impl IntoResponse {
    ws.on_upgrade(|socket| handle_socket(socket, app_state))
}
