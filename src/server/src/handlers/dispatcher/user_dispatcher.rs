use crate::{
    dto::{CreateUserDto, IdDto, UpdateUserDto, UserIdDto},
    handlers::messages::{CommandType, RequestMessage, ResponseMessage, ResponseStatus},
    repositories::user_repository::PgUserRepository,
    services::user_service::UserService,
    state::app_state::AppState,
};

pub async fn dispatch_user_command(
    request: RequestMessage,
    state: AppState,
) -> ResponseMessage {
    let repo = PgUserRepository::new(state.db_pool.clone());
    let service = UserService::new(repo);

    match request.command {
        CommandType::Create => {
            let dto = match serde_json::from_value::<CreateUserDto>(request.payload.clone()) {
                Ok(dto) => dto,
                Err(err) => {
                    return ResponseMessage {
                        request_id: request.request_id,
                        status: ResponseStatus::Error,
                        command: request.command,
                        data: None,
                        error: Some(format!("Invalid payload: {}", err)),
                    };
                }
            };

            match service.create_user(dto).await {
                Ok(user) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(user).ok(),
                    error: None,
                },
                Err(err) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Error,
                    command: request.command,
                    data: None,
                    error: Some(err.to_string()),
                },
            }
        }

        CommandType::Read => {
            let dto = match serde_json::from_value::<IdDto>(request.payload.clone()) {
                Ok(dto) => dto,
                Err(err) => {
                    return ResponseMessage {
                        request_id: request.request_id,
                        status: ResponseStatus::Error,
                        command: request.command,
                        data: None,
                        error: Some(format!("Invalid payload: {}", err)),
                    };
                }
            };

            match service.get_user(dto.id).await {
                Ok(user) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(user).ok(),
                    error: None,
                },
                Err(err) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Error,
                    command: request.command,
                    data: None,
                    error: Some(err.to_string()),
                },
            }
        }

        CommandType::Update => {
            #[derive(serde::Deserialize)]
            struct UpdatePayload {
                id: i32,
                #[serde(flatten)]
                dto: UpdateUserDto,
            }

            let payload = match serde_json::from_value::<UpdatePayload>(request.payload.clone()) {
                Ok(p) => p,
                Err(err) => {
                    return ResponseMessage {
                        request_id: request.request_id,
                        status: ResponseStatus::Error,
                        command: request.command,
                        data: None,
                        error: Some(format!("Invalid payload: {}", err)),
                    };
                }
            };

            match service.update_user(payload.id, payload.dto).await {
                Ok(user) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(user).ok(),
                    error: None,
                },
                Err(err) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Error,
                    command: request.command,
                    data: None,
                    error: Some(err.to_string()),
                },
            }
        }

        CommandType::Delete => {
            let dto = match serde_json::from_value::<IdDto>(request.payload.clone()) {
                Ok(dto) => dto,
                Err(err) => {
                    return ResponseMessage {
                        request_id: request.request_id,
                        status: ResponseStatus::Error,
                        command: request.command,
                        data: None,
                        error: Some(format!("Invalid payload: {}", err)),
                    };
                }
            };

            match service.delete_user(dto.id).await {
                Ok(_) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: Some(serde_json::json!({"deleted": true})),
                    error: None,
                },
                Err(err) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Error,
                    command: request.command,
                    data: None,
                    error: Some(err.to_string()),
                },
            }
        }

        CommandType::List => match service.get_all_users().await {
            Ok(users) => ResponseMessage {
                request_id: request.request_id,
                status: ResponseStatus::Success,
                command: request.command,
                data: serde_json::to_value(users).ok(),
                error: None,
            },
            Err(err) => ResponseMessage {
                request_id: request.request_id,
                status: ResponseStatus::Error,
                command: request.command,
                data: None,
                error: Some(err.to_string()),
            },
        },
    }
}
