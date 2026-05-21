use crate::{
    dto::{CreateGardenDto, IdDto, UpdateGardenDto, UserIdDto},
    handlers::messages::{CommandType, RequestMessage, ResponseMessage, ResponseStatus},
    repositories::garden_repository::PgGardenRepository,
    services::garden_service::GardenService,
    state::app_state::AppState,
};

pub async fn dispatch_garden_command(request: RequestMessage, state: AppState) -> ResponseMessage {
    let repo = PgGardenRepository::new(state.db_pool.clone());
    let service = GardenService::new(repo);

    match request.command {
        CommandType::Create => {
            let dto = match serde_json::from_value::<CreateGardenDto>(request.payload.clone()) {
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

            match service.create_garden(dto).await {
                Ok(garden) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(garden).ok(),
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

            match service.get_garden(dto.id).await {
                Ok(garden) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(garden).ok(),
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
                dto: UpdateGardenDto,
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

            match service.update_garden(payload.id, payload.dto).await {
                Ok(garden) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(garden).ok(),
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

            match service.delete_garden(dto.id).await {
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

        CommandType::List => {
            // Try to parse as UserIdDto for filtering by user_id
            if let Ok(dto) = serde_json::from_value::<UserIdDto>(request.payload.clone()) {
                match service.get_gardens_by_user(dto.user_id).await {
                    Ok(gardens) => ResponseMessage {
                        request_id: request.request_id,
                        status: ResponseStatus::Success,
                        command: request.command,
                        data: serde_json::to_value(gardens).ok(),
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
            } else {
                // If no user_id provided, get all gardens
                match service.get_all_gardens().await {
                    Ok(gardens) => ResponseMessage {
                        request_id: request.request_id,
                        status: ResponseStatus::Success,
                        command: request.command,
                        data: serde_json::to_value(gardens).ok(),
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
        }

        CommandType::Login => ResponseMessage {
            request_id: request.request_id,
            status: ResponseStatus::Error,
            command: request.command,
            data: None,
            error: Some("Unsupported command 'login' for resource 'garden'".to_string()),
        },
    }
}
