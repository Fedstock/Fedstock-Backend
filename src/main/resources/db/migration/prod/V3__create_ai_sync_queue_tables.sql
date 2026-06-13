CREATE TABLE IF NOT EXISTS ai_sync_rounds (
    id BIGSERIAL PRIMARY KEY,
    api_type VARCHAR(50) NOT NULL,
    round_id VARCHAR(120) NOT NULL,
    expected_client_count INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    last_error VARCHAR(500),
    forwarded_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_ai_sync_rounds_api_round
        UNIQUE (api_type, round_id),
    CONSTRAINT chk_ai_sync_rounds_api_type
        CHECK (api_type IN ('CLUSTER_ASSIGNMENT', 'FL_MODEL')),
    CONSTRAINT chk_ai_sync_rounds_status
        CHECK (status IN ('COLLECTING', 'FORWARDING', 'FORWARDED', 'FAILED'))
);

CREATE TABLE IF NOT EXISTS ai_cluster_assignment_queue (
    id BIGSERIAL PRIMARY KEY,
    round_id VARCHAR(120) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    sample_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_ai_cluster_assignment_round_client
        UNIQUE (round_id, client_id)
);

CREATE TABLE IF NOT EXISTS ai_cluster_assignment_feature_names (
    queue_id BIGINT NOT NULL REFERENCES ai_cluster_assignment_queue(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL,
    feature_name VARCHAR(120) NOT NULL,
    PRIMARY KEY (queue_id, sort_order)
);

CREATE TABLE IF NOT EXISTS ai_cluster_assignment_feature_importance (
    queue_id BIGINT NOT NULL REFERENCES ai_cluster_assignment_queue(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL,
    feature_importance NUMERIC(18, 10) NOT NULL,
    PRIMARY KEY (queue_id, sort_order)
);

CREATE TABLE IF NOT EXISTS ai_fl_model_queue (
    id BIGSERIAL PRIMARY KEY,
    round_id VARCHAR(120) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    sample_weight INTEGER,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120),
    model_file BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_ai_fl_model_round_client
        UNIQUE (round_id, client_id)
);
