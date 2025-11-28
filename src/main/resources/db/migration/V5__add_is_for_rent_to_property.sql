-- Add is_for_rent column to property table
ALTER TABLE property ADD COLUMN is_for_rent BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index for filtering by is_for_rent
CREATE INDEX idx_property_is_for_rent ON property(is_for_rent);

