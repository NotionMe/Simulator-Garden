-- Add password field to users table for authentication
ALTER TABLE users ADD COLUMN password_hash TEXT;
