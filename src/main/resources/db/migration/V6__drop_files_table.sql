-- Drop foreign key constraint from property_gallery to files table
ALTER TABLE property_gallery DROP CONSTRAINT IF EXISTS fk_gallery_file;

-- Add foreign key constraint from property_gallery to stored_file table
ALTER TABLE property_gallery 
	ADD CONSTRAINT fk_gallery_stored_file 
	FOREIGN KEY (file_id) REFERENCES stored_file(file_id) ON DELETE CASCADE;

-- Drop the old files table (if exists and empty)
-- Note: This assumes the files table is no longer used
-- If there's data, migrate it first before dropping
DROP TABLE IF EXISTS files;

