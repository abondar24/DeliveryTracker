CREATE TABLE IF NOT EXISTS delivery(
  device_id VARCHAR(64),
  device_sync BIGINT,
  sync_timestamp timestamptz,
  delivered INTEGER,
  distance INTEGER
);
