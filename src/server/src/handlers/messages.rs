use async_trait::async_trait;
use serde::{Deserialize, Serialize};

use crate::errors::error::WsResult;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RequestMessage {
    pub request_id: Option<String>,
    pub command: String,
    pub payload: serde_json::Value,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResponseMessage {
    pub request_id: Option<String>,
    pub status: ResponseStatus,
    pub command: Option<String>,
    pub data: Option<serde_json::Value>,
    pub error: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum ResponseStatus {
    Success,
    Error,
}

#[async_trait]
trait ParseMessage {
    async fn parse_request(input: &str) -> WsResult<RequestMessage>;
    async fn parse_response(input: ResponseMessage) -> WsResult<ResponseMessage>;
}
