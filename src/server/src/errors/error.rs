use thiserror::Error;

#[derive(Error, Debug)]
pub enum AppError {
    #[error("Failed authorization")]
    AuthError,
    #[error("Password empty")]
    PasswordEmpty,
}

pub type AppResult<T> = std::result::Result<T, AppError>;

#[derive(Error, Debug)]
pub enum DatabaseError {
    #[error("Could not connect to database: {0}")]
    ConnectionFailed(#[from] sqlx::Error),

    #[error("Failed to save record to database")]
    FailedToSave,

    #[error("Failed to read record from database")]
    FailedToRead,

    #[error("Invalid credentials")]
    InvalidCredentials,

    #[error("Failed to delete record to database")]
    FailedToDelete,

    #[error("Failed to update database")]
    FailedToUpdate,

    #[error("Record not found")]
    NotFound,
}

pub type DbResult<T> = std::result::Result<T, DatabaseError>;

#[derive(Error, Debug)]
pub enum WebSocketError {
    #[error("Failed parse message {0}")]
    FailedParseMessage(#[from] serde_json::Error),
}

pub type WsResult<T> = std::result::Result<T, WebSocketError>;

#[derive(Error, Debug)]
pub enum CommandError {
    #[error("Failed create object")]
    FailedToCreate,

    #[error("Failed read object")]
    FailedToRead,

    #[error("Failed update object")]
    FailedToUpdate,

    #[error("Failed delete object")]
    FailedToDelete,
}

pub type CmResult<T> = std::result::Result<T, CommandError>;
