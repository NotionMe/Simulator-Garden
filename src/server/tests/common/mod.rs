use sqlx::PgPool;

pub async fn setup_pool() -> PgPool {
    dotenv::dotenv().ok();

    let database_url =
        std::env::var("DATABASE_URL").expect("DATABASE_URL must be set for integration tests");

    let pool = PgPool::connect(&database_url)
        .await
        .expect("Failed to connect to postgres");

    sqlx::migrate!("./db/migrations")
        .run(&pool)
        .await
        .expect("Failed to run migrations");

    pool
}

pub async fn cleanup_all(pool: &PgPool) {
    sqlx::query("DELETE FROM weather_events")
        .execute(pool)
        .await
        .expect("Failed to cleanup weather_events");

    sqlx::query("DELETE FROM gardens")
        .execute(pool)
        .await
        .expect("Failed to cleanup gardens");

    sqlx::query("DELETE FROM users")
        .execute(pool)
        .await
        .expect("Failed to cleanup users");
}

pub async fn seed_user_and_garden(pool: &PgPool) -> i32 {
    let user_id: i32 = sqlx::query_scalar(
        "INSERT INTO users (username, email, password_hash, created_at) VALUES ($1, $2, $3, NOW()) RETURNING id",
    )
    .bind("weather_owner")
    .bind("weather_owner@example.com")
    .bind("hash")
    .fetch_one(pool)
    .await
    .expect("Failed to create user");

    sqlx::query_scalar(
        "INSERT INTO gardens (user_id, name, width_cells, height_cells, created_at) VALUES ($1, $2, $3, $4, NOW()) RETURNING id",
    )
    .bind(user_id)
    .bind("Weather Test Garden")
    .bind(10)
    .bind(10)
    .fetch_one(pool)
    .await
    .expect("Failed to create garden")
}
