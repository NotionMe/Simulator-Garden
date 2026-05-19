mod common;

use chrono::Utc;
use server::{
    models::User,
    repositories::{
        repository::Repository,
        user_repository::{PgUserRepository, UserRepository},
    },
};

fn sample_user(username: &str, email: &str) -> User {
    User {
        id: 0,
        username: username.to_string(),
        email: email.to_string(),
        password_hash: "hashed_password".to_string(),
        created_at: Utc::now().naive_utc(),
    }
}

#[tokio::test]
async fn create_and_find_by_id_should_work() {
    let pool = common::setup_pool().await;
    common::cleanup_all(&pool).await;

    let repo = PgUserRepository::new(pool.clone());

    let created = repo
        .create(sample_user("u_create_id", "u_create_id@example.com"))
        .await
        .expect("create failed");

    let found = repo
        .find_by_id(created.id)
        .await
        .expect("find_by_id failed");
    let found = found.expect("Expected user to exist");

    assert_eq!(found.username, "u_create_id");
    assert_eq!(found.email, "u_create_id@example.com");
}

#[tokio::test]
async fn lookup_and_exists_should_work() {
    let pool = common::setup_pool().await;
    common::cleanup_all(&pool).await;

    let repo = PgUserRepository::new(pool.clone());
    repo.create(sample_user("u_lookup", "u_lookup@example.com"))
        .await
        .expect("create failed");

    let by_username = repo
        .find_by_username("u_lookup")
        .await
        .expect("find_by_username failed");
    let by_email = repo
        .find_by_email("u_lookup@example.com")
        .await
        .expect("find_by_email failed");
    let username_exists = repo
        .exists_by_username("u_lookup")
        .await
        .expect("exists_by_username failed");
    let email_exists = repo
        .exists_by_email("u_lookup@example.com")
        .await
        .expect("exists_by_email failed");

    assert!(by_username.is_some());
    assert!(by_email.is_some());
    assert!(username_exists);
    assert!(email_exists);
}

#[tokio::test]
async fn update_and_delete_should_work() {
    let pool = common::setup_pool().await;
    common::cleanup_all(&pool).await;

    let repo = PgUserRepository::new(pool.clone());
    let created = repo
        .create(sample_user("u_before", "u_before@example.com"))
        .await
        .expect("create failed");

    let updated = repo
        .update(
            created.id,
            User {
                id: created.id,
                username: "u_after".to_string(),
                email: "u_after@example.com".to_string(),
                password_hash: "new_hash".to_string(),
                created_at: created.created_at,
            },
        )
        .await
        .expect("update failed");

    assert_eq!(updated.username, "u_after");
    assert_eq!(updated.email, "u_after@example.com");

    let deleted = repo.delete(created.id).await.expect("delete failed");
    assert!(deleted);

    let after_delete = repo
        .find_by_id(created.id)
        .await
        .expect("find_by_id after delete failed");
    assert!(after_delete.is_none());
}
