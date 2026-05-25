use axum::{
    Json,
    extract::{
        Query, State, WebSocketUpgrade,
        ws::{Message, WebSocket},
    },
    http::StatusCode,
    response::{IntoResponse, Response},
};
use serde::Deserialize;
use serde_json::json;
use tracing::{info, warn};

use crate::{
    dto::LoginUserDto,
    handlers::{
        dispatcher::dispatch_request,
        messages::{
            AuthorizationRequestMessage, AuthorizationResponeMessage, RequestMessage,
            ResponseMessage, ResponseStatus,
        },
    },
    repositories::user_repository::PgUserRepository,
    services::auth::jwt_service::JwtClaims,
    services::user_service::UserService,
    state::app_state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct WsAuthQuery {
    pub token: String,
}

pub async fn handle_socket(mut socket: WebSocket, state: AppState, claims: JwtClaims) {
    info!(
        user_id = %claims.sub,
        username = %claims.username,
        "WebSocket session started"
    );

    while let Some(Ok(msg)) = socket.recv().await {
        if let Message::Text(text) = msg {
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
                            serde_json::to_string(&response).unwrap_or_default().into(),
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

            let _ = socket.send(Message::Text(response_text.into())).await;
        }
    }

    info!(
        user_id = %claims.sub,
        username = %claims.username,
        "WebSocket session closed"
    );
}

pub async fn handler(
    ws: WebSocketUpgrade,
    Query(auth): Query<WsAuthQuery>,
    State(state): State<AppState>,
) -> Response {
    if auth.token.trim().is_empty() {
        return unauthorized("Missing token");
    }

    match state.jwt.validate_token(&auth.token) {
        Ok(claims) => ws
            .on_upgrade(move |socket| handle_socket(socket, state, claims))
            .into_response(),
        Err(err) => {
            warn!(?err, "Rejected WebSocket connection: invalid token");
            unauthorized("Invalid or expired token")
        }
    }
}

pub async fn post_authorization(
    State(state): State<AppState>,
    Json(body): Json<AuthorizationRequestMessage>,
) -> impl IntoResponse {
    let repo = PgUserRepository::new(state.db_pool.clone());
    let service = UserService::new(repo);

    let dto = match serde_json::from_value::<LoginUserDto>(body.payload.clone()) {
        Ok(dto) => dto,
        Err(err) => {
            return Json(AuthorizationResponeMessage {
                request_id: body.request_id,
                reason: body.reason,
                payload: None,
                error: Some(format!("Failed payload: {}", err)),
            });
        }
    };

    match service.login_user(dto).await {
        Ok(user) => match state.jwt.issue_token(&user) {
            Ok(token) => Json(AuthorizationResponeMessage {
                request_id: body.request_id,
                reason: "Success authorization".into(),
                payload: Some(json!({
                    "token": token,
                    "public_key": state.signing_keys.public_key_hex(),
                    "user": user,
                })),
                error: None,
            }),
            Err(err) => Json(AuthorizationResponeMessage {
                request_id: body.request_id,
                reason: "Failed".into(),
                payload: None,
                error: Some(err.to_string()),
            }),
        },
        Err(err) => Json(AuthorizationResponeMessage {
            request_id: body.request_id,
            reason: "Failed".into(),
            payload: None,
            error: Some(err.to_string()),
        }),
    }
}

fn unauthorized(message: &'static str) -> Response {
    (StatusCode::UNAUTHORIZED, message).into_response()
}
