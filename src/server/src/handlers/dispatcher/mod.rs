pub mod user_dispatcher;
pub mod garden_dispatcher;
pub mod plant_dispatcher;
pub mod plant_instance_dispatcher;
pub mod task_dispatcher;
pub mod achievement_dispatcher;
pub mod weather_event_dispatcher;

use crate::{
    handlers::messages::{RequestMessage, ResourceType, ResponseMessage, ResponseStatus},
    state::app_state::AppState,
};

pub async fn dispatch_request(request: RequestMessage, state: AppState) -> ResponseMessage {
    match request.reason {
        ResourceType::User => user_dispatcher::dispatch_user_command(request, state).await,
        ResourceType::Garden => garden_dispatcher::dispatch_garden_command(request, state).await,
        ResourceType::Plant => plant_dispatcher::dispatch_plant_command(request, state).await,
        ResourceType::PlantInstance => {
            plant_instance_dispatcher::dispatch_plant_instance_command(request, state).await
        }
        ResourceType::Task => task_dispatcher::dispatch_task_command(request, state).await,
        ResourceType::Achievement => {
            achievement_dispatcher::dispatch_achievement_command(request, state).await
        }
        ResourceType::WeatherEvent => {
            weather_event_dispatcher::dispatch_weather_event_command(request, state).await
        }
    }
}
