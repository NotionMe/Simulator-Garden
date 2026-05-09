-- GARDEN GAME DATABASE - SEED DATA (SQLite)

-- INSERT: Sample Data

INSERT INTO users (username, email, created_at) VALUES
('player_john', 'john@example.com', '2026-05-01 10:30:00'),
('player_alice', 'alice@example.com', '2026-05-02 14:45:00'),
('player_bob', 'bob@example.com', '2026-05-03 09:15:00');

INSERT INTO plants (name, first_name, last_name, species, growth_days, climate_type, icon_key) VALUES
('Tomato', NULL, NULL, 'Solanum lycopersicum', 60, 'temperate', 'tomato_icon'),
('Carrot', NULL, NULL, 'Daucus carota', 70, 'temperate', 'carrot_icon'),
('Banana', NULL, NULL, 'Musa acuminata', 90, 'tropical', 'banana_icon'),
('Cactus', NULL, NULL, 'Cactaceae', 120, 'arid', 'cactus_icon'),
('Lettuce', NULL, NULL, 'Lactuca sativa', 45, 'temperate', 'lettuce_icon');

INSERT INTO gardens (user_id, name, width_cells, height_cells, created_at) VALUES
(1, 'My First Garden', 10, 10, '2026-05-05 11:00:00'),
(1, 'Summer Garden', 15, 15, '2026-05-06 08:30:00'),
(2, 'Alice''s Garden', 12, 12, '2026-05-07 15:20:00'),
(3, 'Bob''s Tropical Garden', 20, 20, '2026-05-08 10:45:00');

INSERT INTO plant_instances (garden_id, plant_id, cell_x, cell_y, planted_at, growth_stage, is_watered, is_fertilized) VALUES
(1, 1, 0, 0, '2026-05-05', 50, 1, 0),
(1, 2, 1, 0, '2026-05-05', 35, 1, 1),
(1, 5, 2, 0, '2026-05-06', 20, 0, 0),
(2, 3, 0, 0, '2026-05-06', 45, 1, 0),
(3, 1, 5, 5, '2026-05-07', 60, 1, 1),
(4, 4, 10, 10, '2026-05-08', 80, 0, 1);

INSERT INTO tasks (plant_instance_id, task_type, due_at, is_done) VALUES
(1, 'water', '2026-05-10 18:00:00', 0),
(1, 'fertilize', '2026-05-12 18:00:00', 0),
(2, 'water', '2026-05-11 18:00:00', 1),
(3, 'water', '2026-05-11 18:00:00', 0),
(4, 'harvest', '2026-05-15 18:00:00', 0),
(5, 'prune', '2026-05-13 18:00:00', 0),
(6, 'water', '2026-05-12 18:00:00', 0);

INSERT INTO weather_events (garden_id, event_type, intensity, occurred_at) VALUES
(1, 'rain', 7, '2026-05-08 14:30:00'),
(1, 'heatwave', 8, '2026-05-08 12:00:00'),
(2, 'drought', 6, '2026-05-07 10:00:00'),
(3, 'rain', 5, '2026-05-06 09:15:00'),
(4, 'storm', 9, '2026-05-05 16:45:00');

INSERT INTO achievements (user_id, title, condition_key, earned_at) VALUES
(1, 'First Harvest', 'harvested_first_plant', '2026-05-06 10:30:00'),
(1, 'Green Thumb', 'planted_10_plants', '2026-05-07 14:45:00'),
(2, 'First Harvest', 'harvested_first_plant', '2026-05-07 11:00:00'),
(3, 'Tropical Master', 'planted_tropical_plant', '2026-05-08 09:30:00');

INSERT INTO garden_plants (garden_id, plant_id, added_at) VALUES
(1, 1, '2026-05-05 11:00:00'),
(1, 2, '2026-05-05 11:05:00'),
(1, 5, '2026-05-06 10:30:00'),
(2, 3, '2026-05-06 08:30:00'),
(2, 1, '2026-05-06 09:00:00'),
(3, 1, '2026-05-07 15:20:00'),
(4, 4, '2026-05-08 10:45:00'),
(4, 3, '2026-05-08 11:15:00');
