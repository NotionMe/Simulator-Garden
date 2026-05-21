use crate::{
    dto::{
        CreateWeatherEventDto, GardenIdDto, IdDto, UpdateWeatherEventDto, WeatherEventResponseDto,
    },
    handlers::messages::{CommandType, RequestMessage, ResponseMessage, ResponseStatus},
    repositories::weather_event_repository::PgWeatherEventRepository,
    services::weather_event_service::WeatherEventService,
    state::app_state::AppState,
};

pub async fn dispatch_weather_event_command(
    request: RequestMessage,
    state: AppState,
) -> ResponseMessage {
    let repo = PgWeatherEventRepository::new(state.db_pool.clone());
    let service = WeatherEventService::new(repo);

    match request.command {
        CommandType::Create => {
            let dto = match serde_json::from_value::<CreateWeatherEventDto>(request.payload.clone())
            {
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

            match service.create_weather_event(dto).await {
                Ok(weather_event) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(weather_event).ok(),
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

            match service.get_weather_event(dto.id).await {
                Ok(weather_event) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(weather_event).ok(),
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
                dto: UpdateWeatherEventDto,
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

            match service.update_weather_event(payload.id, payload.dto).await {
                Ok(weather_event) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(weather_event).ok(),
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

            match service.delete_weather_event(dto.id).await {
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
            let result =
                if let Ok(dto) = serde_json::from_value::<GardenIdDto>(request.payload.clone()) {
                    service.get_weather_events_by_garden(dto.garden_id).await
                } else {
                    service.get_all_weather_events().await
                };

            match result {
                Ok(weather_events) => ResponseMessage {
                    request_id: request.request_id,
                    status: ResponseStatus::Success,
                    command: request.command,
                    data: serde_json::to_value(weather_events).ok(),
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

        CommandType::Login => ResponseMessage {
            request_id: request.request_id,
            status: ResponseStatus::Error,
            command: request.command,
            data: None,
            error: Some("Unsupported command 'login' for resource 'weatherevent'".to_string()),
        },
    }
}
