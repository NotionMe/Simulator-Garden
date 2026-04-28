-- GARDEN GAME DATABASE - DML (SQLite)

-- INSERT: Sample Data

INSERT INTO users (username, email, created_at) VALUES
('player_john', 'john@example.com', '2026-01-15 10:30:00'),
('player_alice', 'alice@example.com', '2026-01-20 14:45:00'),
('player_bob', 'bob@example.com', '2026-02-01 09:15:00');

INSERT INTO plants (name, first_name, last_name, species, growth_days, climate_type, icon_key) VALUES
('Tomato', 'Tom', 'Ato', 'Solanum lycopersicum', 60, 'temperate', 'tomato_icon'),
('Carrot', 'Car', 'Rot', 'Daucus carota', 70, 'temperate', 'carrot_icon'),
('Banana', 'Ban', 'Ana', 'Musa acuminata', 90, 'tropical', 'banana_icon'),
('Cactus', 'Cac', 'Tus', 'Cactaceae', 120, 'arid', 'cactus_icon'),
('Lettuce', 'Let', 'Tuce', 'Lactuca sativa', 45, 'temperate', 'lettuce_icon');

INSERT INTO gardens (user_id, name, width_cells, height_cells, created_at) VALUES
(1, 'My First Garden', 10, 10, '2026-01-16 11:00:00'),
(1, 'Summer Garden', 15, 15, '2026-02-10 08:30:00'),
(2, 'Alice''s Garden', 12, 12, '2026-01-21 15:20:00'),
(3, 'Bob''s Tropical Garden', 20, 20, '2026-02-02 10:45:00');

INSERT INTO plant_instances (garden_id, plant_id, cell_x, cell_y, planted_at, growth_stage, is_watered, is_fertilized) VALUES
(1, 1, 0, 0, '2026-01-16', 50, 1, 0),
(1, 2, 1, 0, '2026-01-16', 35, 1, 1),
(1, 5, 2, 0, '2026-01-20', 20, 0, 0),
(2, 3, 0, 0, '2026-02-10', 45, 1, 0),
(3, 1, 5, 5, '2026-01-22', 60, 1, 1),
(4, 4, 10, 10, '2026-02-03', 80, 0, 1);

INSERT INTO tasks (plant_instance_id, task_type, due_at, is_done) VALUES
(1, 'water', '2026-04-28 18:00:00', 0),
(1, 'fertilize', '2026-05-05 18:00:00', 0),
(2, 'water', '2026-04-28 18:00:00', 1),
(3, 'water', '2026-04-29 18:00:00', 0),
(4, 'harvest', '2026-05-10 18:00:00', 0),
(5, 'prune', '2026-05-01 18:00:00', 0),
(6, 'water', '2026-04-30 18:00:00', 0);

INSERT INTO weather_events (garden_id, event_type, intensity, occurred_at) VALUES
(1, 'rain', 7, '2026-04-25 14:30:00'),
(1, 'heatwave', 8, '2026-04-26 12:00:00'),
(2, 'drought', 6, '2026-04-27 10:00:00'),
(3, 'rain', 5, '2026-04-28 09:15:00'),
(4, 'storm', 9, '2026-04-24 16:45:00');

INSERT INTO achievements (user_id, title, condition_key, earned_at) VALUES
(1, 'First Harvest', 'harvested_first_plant', '2026-02-15 10:30:00'),
(1, 'Green Thumb', 'planted_10_plants', '2026-03-20 14:45:00'),
(2, 'First Harvest', 'harvested_first_plant', '2026-02-28 11:00:00'),
(3, 'Tropical Master', 'planted_tropical_plant', '2026-02-10 09:30:00');

INSERT INTO garden_plants (garden_id, plant_id, added_at) VALUES
(1, 1, '2026-01-16 11:00:00'),
(1, 2, '2026-01-16 11:05:00'),
(1, 5, '2026-01-20 10:30:00'),
(2, 3, '2026-02-10 08:30:00'),
(2, 1, '2026-02-11 09:00:00'),
(3, 1, '2026-01-21 15:20:00'),
(4, 4, '2026-02-02 10:45:00'),
(4, 3, '2026-02-03 11:15:00');

-- SELECT: Query Examples

-- Get all gardens for user with plant count (1:N)
SELECT
    g.id, g.name, g.width_cells, g.height_cells,
    COUNT(pi.id) AS plant_count, g.created_at
FROM gardens g
LEFT JOIN plant_instances pi ON g.id = pi.garden_id
WHERE g.user_id = 1
GROUP BY g.id, g.name, g.width_cells, g.height_cells, g.created_at
ORDER BY g.created_at DESC;

-- Get all plant instances in garden with details (1:N)
SELECT
    pi.id, pi.cell_x, pi.cell_y, p.name, p.species,
    pi.growth_stage, pi.is_watered, pi.is_fertilized, pi.planted_at
FROM plant_instances pi
JOIN plants p ON pi.plant_id = p.id
WHERE pi.garden_id = 1
ORDER BY pi.cell_x, pi.cell_y;

-- Get pending tasks for plants in garden (1:N)
SELECT
    t.id, p.name, t.task_type, t.due_at, t.is_done, pi.cell_x, pi.cell_y
FROM tasks t
JOIN plant_instances pi ON t.plant_instance_id = pi.id
JOIN plants p ON pi.plant_id = p.id
WHERE pi.garden_id = 1 AND t.is_done = 0
ORDER BY t.due_at ASC;

-- Get all plants available in garden (M:N via junction table)
SELECT DISTINCT
    p.id, p.name, p.species, p.growth_days, p.climate_type, gp.added_at
FROM plants p
JOIN garden_plants gp ON p.id = gp.plant_id
WHERE gp.garden_id = 1
ORDER BY p.name;

-- Get weather events for garden (1:N)
SELECT
    we.id, we.event_type, we.intensity, we.occurred_at, g.name
FROM weather_events we
JOIN gardens g ON we.garden_id = g.id
WHERE we.garden_id = 1
ORDER BY we.occurred_at DESC;

-- Get user achievements (1:N)
SELECT
    a.id, a.title, a.condition_key, a.earned_at, u.username
FROM achievements a
JOIN users u ON a.user_id = u.id
WHERE a.user_id = 1
ORDER BY a.earned_at DESC;

-- User garden statistics (multiple 1:N)
SELECT
    u.username,
    COUNT(DISTINCT g.id) AS total_gardens,
    COUNT(DISTINCT pi.id) AS total_plants,
    COUNT(DISTINCT t.id) AS total_tasks,
    SUM(CASE WHEN t.is_done = 1 THEN 1 ELSE 0 END) AS completed_tasks,
    COUNT(DISTINCT a.id) AS total_achievements
FROM users u
LEFT JOIN gardens g ON u.id = g.user_id
LEFT JOIN plant_instances pi ON g.id = pi.garden_id
LEFT JOIN tasks t ON pi.id = t.plant_instance_id
LEFT JOIN achievements a ON u.id = a.user_id
WHERE u.id = 1
GROUP BY u.id, u.username;

-- UPDATE: Data Modification

UPDATE tasks SET is_done = 1 WHERE id = 1;
UPDATE plant_instances SET is_watered = 1 WHERE id = 1;
UPDATE plant_instances SET growth_stage = 75 WHERE id = 1;
UPDATE plant_instances SET is_fertilized = 1 WHERE garden_id = 1 AND is_fertilized = 0;

-- DELETE: Data Removal

DELETE FROM tasks WHERE id = 1 AND is_done = 1;
DELETE FROM plant_instances WHERE id = 1;
DELETE FROM gardens WHERE id = 1;
