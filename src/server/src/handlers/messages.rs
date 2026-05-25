use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuthorizationRequestMessage {
    pub request_id: Option<String>,
    pub reason: String,
    pub payload: serde_json::Value,
    pub error: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuthorizationResponeMessage {
    pub request_id: Option<String>,
    pub reason: String,
    pub payload: Option<serde_json::Value>,
    pub error: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RequestMessage {
    pub request_id: Option<String>,
    pub command: CommandType,
    pub reason: ResourceType,
    pub payload: serde_json::Value,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResponseMessage {
    pub request_id: Option<String>,
    pub status: ResponseStatus,
    pub command: CommandType,
    pub data: Option<serde_json::Value>,
    pub error: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum ResponseStatus {
    Success,
    Error,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum CommandType {
    Create,
    Read,
    Update,
    Delete,
    List,
    Login,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum ResourceType {
    User,
    Garden,
    Plant,
    PlantInstance,
    Task,
    Achievement,
    WeatherEvent,
    PlayerInventoryItem,
}
