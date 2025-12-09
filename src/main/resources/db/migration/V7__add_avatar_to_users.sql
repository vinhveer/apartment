-- Add avatar column to users table (stores base64 encoded compressed image)
ALTER TABLE users ADD COLUMN avatar TEXT;

COMMENT ON COLUMN users.avatar IS 'Base64 encoded avatar image (compressed)';
