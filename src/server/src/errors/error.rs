use thiserror::Error;

#[derive(Error, Debug)]
pub enum AppError {}

pub type AppResult<T> = std::result::Result<T, AppError>;

#[derive(Error, Debug)]
pub enum DatabaseError {
    #[error("Could not connect to database: {0}")]
    ConnectionFailed(#[from] sqlx::Error),

    #[error("Record not found")]
    NotFound,
}

pub type DbResult<T> = std::result::Result<T, DatabaseError>;

#[derive(Error, Debug)]

pub enum WebSocketError {}
pub type WsResult<T> = std::result::Result<T, WebSocketError>;
