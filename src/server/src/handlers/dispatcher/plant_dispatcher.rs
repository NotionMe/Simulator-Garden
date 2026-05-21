use crate::{
    dto::{CreatePlantDto, IdDto, UpdatePlantDto},
    handlers::messages::{CommandType, RequestMessage, ResponseMessage, ResponseStatus},
    repositories::plant_repository::PgPlantRepository,
    services::plant_service::PlantService,
    state::app_state::AppState,
};

pub async fn dispatch_plant_command(request: RequestMessage, state: AppState) -> ResponseMessage {
    let repo = PgPlantRepository::new(state.db_pool.clone());
    let service = PlantService::new(repo);

    match request.command {
        CommandType::Create => {
            let dto = match serde_json::from_value::<CreatePlantDto>(request.payload.clone()) {
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

            match service.create_plant(dto).await {
                Ok(plant) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(plant).ok(),
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

            match service.get_plant(dto.id).await {
                Ok(plant) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(plant).ok(),
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
                dto: UpdatePlantDto,
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

            match service.update_plant(payload.id, payload.dto).await {
                Ok(plant) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(plant).ok(),
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

            match service.delete_plant(dto.id).await {
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

        CommandType::List => match service.get_all_plants().await {
            Ok(plants) => ResponseMessage {
                request_id: request.request_id,
                status: ResponseStatus::Success,
                command: request.command,
                data: serde_json::to_value(plants).ok(),
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

        CommandType::Login => ResponseMessage {
            request_id: request.request_id,
            status: ResponseStatus::Error,
            command: request.command,
            data: None,
            error: Some("Unsupported command 'login' for resource 'plant'".to_string()),
        },
    }
}
