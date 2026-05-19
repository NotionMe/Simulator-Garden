use async_trait::async_trait;
use serde_json::from_value;

use crate::{
    errors::error::CmResult,
    handlers::messages::{CommandType, RequestMessage, ResourceType, ResponseMessage},
    models::User,
};

pub struct CommandUser {
    pub command: CommandType,
    pub reason: ResourceType,
    pub request: RequestMessage,
    pub response: ResponseMessage,
}

#[async_trait]
trait Command {
    async fn create(&self) -> CmResult<User>;
    async fn read(&self) -> CmResult<User>;
    async fn update(&self) -> CmResult<User>;
    async fn delete(&self) -> CmResult<bool>;
}

#[async_trait]
impl Command for CommandUser {
    async fn create(&self) -> CmResult<User> {
        let request = &self.request;
        let payload = request.payload.clone();

        let user: User = from_value(payload).expect("Error parse to User");
        todo!()
    }

    async fn read(&self) -> CmResult<User> {
        todo!()
    }

    async fn update(&self) -> CmResult<User> {
        todo!()
    }

    async fn delete(&self) -> CmResult<bool> {
        todo!()
    }
}
