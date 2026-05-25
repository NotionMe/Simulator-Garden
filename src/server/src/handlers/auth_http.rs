use axum::{Json, extract::State, response::IntoResponse};
use serde_json::json;

use crate::{
    dto::{CreateUserDto, LoginUserDto, UserResponseDto},
    handlers::messages::{AuthorizationRequestMessage, AuthorizationResponeMessage},
    repositories::user_repository::PgUserRepository,
    services::user_service::UserService,
    state::app_state::AppState,
};

pub async fn post_authorization(
    State(state): State<AppState>,
    Json(body): Json<AuthorizationRequestMessage>,
) -> impl IntoResponse {
    let dto = match parse_payload::<LoginUserDto>(&body) {
        Ok(dto) => dto,
        Err(response) => return response,
    };

    let repo = PgUserRepository::new(state.db_pool.clone());
    let service = UserService::new(repo);

    match service.login_user(dto).await {
        Ok(user) => Json(auth_success(
            body.request_id,
            "Success authorization",
            user,
            &state,
        )),
        Err(err) => Json(auth_failure(body.request_id, err.to_string())),
    }
}

pub async fn post_registration(
    State(state): State<AppState>,
    Json(body): Json<AuthorizationRequestMessage>,
) -> impl IntoResponse {
    let dto = match parse_payload::<CreateUserDto>(&body) {
        Ok(dto) => dto,
        Err(response) => return response,
    };

    let repo = PgUserRepository::new(state.db_pool.clone());
    let service = UserService::new(repo);

    match service.create_user(dto).await {
        Ok(user) => Json(auth_success(
            body.request_id,
            "Success registration",
            user,
            &state,
        )),
        Err(err) => Json(auth_failure(body.request_id, err.to_string())),
    }
}

fn parse_payload<T: serde::de::DeserializeOwned>(
    body: &AuthorizationRequestMessage,
) -> Result<T, Json<AuthorizationResponeMessage>> {
    serde_json::from_value::<T>(body.payload.clone()).map_err(|err| {
        Json(AuthorizationResponeMessage {
            request_id: body.request_id.clone(),
            reason: body.reason.clone(),
            payload: None,
            error: Some(format!("Failed payload: {}", err)),
        })
    })
}

fn auth_success(
    request_id: Option<String>,
    reason: &str,
    user: UserResponseDto,
    state: &AppState,
) -> AuthorizationResponeMessage {
    match state.jwt.issue_token(&user) {
        Ok(token) => AuthorizationResponeMessage {
            request_id,
            reason: reason.into(),
            payload: Some(json!({
                "token": token,
                "public_key": state.signing_keys.public_key_hex(),
                "user": user,
            })),
            error: None,
        },
        Err(err) => auth_failure(request_id, err.to_string()),
    }
}

fn auth_failure(request_id: Option<String>, error: String) -> AuthorizationResponeMessage {
    AuthorizationResponeMessage {
        request_id,
        reason: "Failed".into(),
        payload: None,
        error: Some(error),
    }
}
