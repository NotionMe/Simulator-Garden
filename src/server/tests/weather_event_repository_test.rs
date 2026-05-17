mod common;

use chrono::{Duration, Utc};
use server::{
    models::{EventType, WeatherEvent},
    repositories::{
        repository::Repository,
        weather_event_repository::{PgWeatherEventRepository, WeatherEventRepository},
    },
};

fn sample_event(garden_id: i32, event_type: EventType, intensity: i32) -> WeatherEvent {
    WeatherEvent {
        id: 0,
        garden_id,
        event_type,
        intensity,
        occurred_at: Utc::now(),
    }
}

#[tokio::test]
async fn create_and_find_all_should_work() {
    let pool = common::setup_pool().await;
    common::cleanup_all(&pool).await;

    let garden_id = common::seed_user_and_garden(&pool).await;
    let repo = PgWeatherEventRepository::new(pool.clone());

    repo.create(sample_event(garden_id, EventType::Rain, 5))
        .await
        .expect("create weather event failed");

    let all = repo.find_all().await.expect("find_all failed");

    assert_eq!(all.len(), 1);
    assert_eq!(all[0].garden_id, garden_id);
}

#[tokio::test]
async fn finders_by_garden_and_type_should_work() {
    let pool = common::setup_pool().await;
    common::cleanup_all(&pool).await;

    let garden_id = common::seed_user_and_garden(&pool).await;
    let repo = PgWeatherEventRepository::new(pool.clone());

    repo.create(sample_event(garden_id, EventType::Rain, 4))
        .await
        .expect("create rain event failed");
    repo.create(sample_event(garden_id, EventType::Storm, 8))
        .await
        .expect("create storm event failed");

    let by_garden = repo
        .find_by_garden_id(garden_id)
        .await
        .expect("find_by_garden_id failed");
    let by_type = repo
        .find_by_event_type(EventType::Rain)
        .await
        .expect("find_by_event_type failed");
    let by_both = repo
        .find_by_garden_id_and_event_type(garden_id, EventType::Storm)
        .await
        .expect("find_by_garden_id_and_event_type failed");

    assert_eq!(by_garden.len(), 2);
    assert_eq!(by_type.len(), 1);
    assert_eq!(by_both.len(), 1);
    assert!(matches!(by_type[0].event_type, EventType::Rain));
}

#[tokio::test]
async fn find_by_garden_id_since_should_filter_old_records() {
    let pool = common::setup_pool().await;
    common::cleanup_all(&pool).await;

    let garden_id = common::seed_user_and_garden(&pool).await;
    let repo = PgWeatherEventRepository::new(pool.clone());

    let now = Utc::now();

    repo.create(WeatherEvent {
        id: 0,
        garden_id,
        event_type: EventType::Sun,
        intensity: 3,
        occurred_at: now - Duration::hours(2),
    })
    .await
    .expect("create old event failed");

    repo.create(WeatherEvent {
        id: 0,
        garden_id,
        event_type: EventType::Sun,
        intensity: 7,
        occurred_at: now,
    })
    .await
    .expect("create new event failed");

    let recent = repo
        .find_by_garden_id_since(garden_id, now - Duration::minutes(30))
        .await
        .expect("find_by_garden_id_since failed");

    assert_eq!(recent.len(), 1);
    assert_eq!(recent[0].intensity, 7);
}

#[tokio::test]
async fn delete_should_remove_record() {
    let pool = common::setup_pool().await;
    common::cleanup_all(&pool).await;

    let garden_id = common::seed_user_and_garden(&pool).await;
    let repo = PgWeatherEventRepository::new(pool.clone());

    let created = repo
        .create(sample_event(garden_id, EventType::Frost, 6))
        .await
        .expect("create frost event failed");

    let deleted = repo.delete(created.id).await.expect("delete failed");

    assert!(deleted);
}
