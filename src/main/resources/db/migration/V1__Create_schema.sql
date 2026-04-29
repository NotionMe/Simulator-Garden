-- GARDEN GAME DATABASE - DDL (SQLite)

-- users: Independent Entity, 3NF
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHECK (LENGTH(username) >= 3),
    CHECK (email LIKE '%@%.%')
);

-- gardens: Weak Entity (depends on users), 3NF
CREATE TABLE gardens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    width_cells INTEGER NOT NULL,
    height_cells INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (width_cells > 0 AND width_cells <= 100),
    CHECK (height_cells > 0 AND height_cells <= 100)
);

-- plants: Independent Entity (Reference Data), 3NF
CREATE TABLE plants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    first_name TEXT,
    last_name TEXT,
    species TEXT NOT NULL,
    growth_days INTEGER NOT NULL,
    climate_type TEXT NOT NULL,
    icon_key TEXT,
    CHECK (growth_days > 0),
    CHECK (climate_type IN ('temperate', 'tropical', 'arid'))
);

-- plant_instances: Weak Entity (depends on gardens & plants), 3NF
CREATE TABLE plant_instances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    garden_id INTEGER NOT NULL,
    plant_id INTEGER NOT NULL,
    cell_x INTEGER NOT NULL,
    cell_y INTEGER NOT NULL,
    planted_at DATE NOT NULL,
    growth_stage INTEGER NOT NULL DEFAULT 0,
    is_watered BOOLEAN DEFAULT 0,
    is_fertilized BOOLEAN DEFAULT 0,
    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
    FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE RESTRICT,
    UNIQUE (garden_id, cell_x, cell_y),
    CHECK (growth_stage >= 0 AND growth_stage <= 100)
);

-- tasks: Weak Entity (depends on plant_instances), 3NF
CREATE TABLE tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plant_instance_id INTEGER NOT NULL,
    task_type TEXT NOT NULL,
    due_at DATETIME NOT NULL,
    is_done BOOLEAN DEFAULT 0,
    FOREIGN KEY (plant_instance_id) REFERENCES plant_instances(id) ON DELETE CASCADE,
    CHECK (task_type IN ('water', 'fertilize', 'harvest', 'prune'))
);

-- weather_events: Weak Entity (depends on gardens), 3NF
CREATE TABLE weather_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    garden_id INTEGER NOT NULL,
    event_type TEXT NOT NULL,
    intensity INTEGER NOT NULL,
    occurred_at DATETIME NOT NULL,
    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
    CHECK (event_type IN ('rain', 'drought', 'frost', 'heatwave', 'storm')),
    CHECK (intensity >= 1 AND intensity <= 10)
);

-- achievements: Weak Entity (depends on users), 3NF
CREATE TABLE achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    condition_key TEXT NOT NULL,
    earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user_id, condition_key)
);

-- garden_plants: Junction Table (M:N relationship), 3NF
CREATE TABLE garden_plants (
    garden_id INTEGER NOT NULL,
    plant_id INTEGER NOT NULL,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (garden_id, plant_id),
    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
    FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_gardens_user_id ON gardens(user_id);
CREATE INDEX idx_plant_instances_garden_id ON plant_instances(garden_id);
CREATE INDEX idx_plant_instances_plant_id ON plant_instances(plant_id);
CREATE INDEX idx_tasks_plant_instance_id ON tasks(plant_instance_id);
CREATE INDEX idx_weather_events_garden_id ON weather_events(garden_id);
CREATE INDEX idx_achievements_user_id ON achievements(user_id);
