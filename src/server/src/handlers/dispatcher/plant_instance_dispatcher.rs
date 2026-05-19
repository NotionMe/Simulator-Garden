use crate::{
    dto::{CreatePlantInstanceDto, GardenIdDto, IdDto, UpdatePlantInstanceDto},
    handlers::messages::{CommandType, RequestMessage, ResponseMessage, ResponseStatus},
    repositories::plant_instance_repository::PgPlantInstanceRepository,
    services::plant_instance_service::PlantInstanceService,
    state::app_state::AppState,
};

pub async fn dispatch_plant_instance_command(
    request: RequestMessage,
    state: AppState,
) -> ResponseMessage {
    let repo = PgPlantInstanceRepository::new(state.db_pool.clone());
    let service = PlantInstanceService::new(repo);

    match request.command {
        CommandType::Create => {
            let dto = match serde_json::from_value::<CreatePlantInstanceDto>(request.payload.clone()) {
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

            match service.create_plant_instance(dto).await {
                Ok(plant_instance) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(plant_instance).ok(),
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

            match service.get_plant_instance(dto.id).await {
                Ok(plant_instance) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(plant_instance).ok(),
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
                dto: UpdatePlantInstanceDto,
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

            match service.update_plant_instance(payload.id, payload.dto).await {
                Ok(plant_instance) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(plant_instance).ok(),
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

            match service.delete_plant_instance(dto.id).await {
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
            // Try to parse as GardenIdDto for filtering, otherwise get all
            let result = if let Ok(garden_dto) = serde_json::from_value::<GardenIdDto>(request.payload.clone()) {
                service.get_plant_instances_by_garden(garden_dto.garden_id).await
            } else {
                service.get_all_plant_instances().await
            };

            match result {
                Ok(plant_instances) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(plant_instances).ok(),
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
}
