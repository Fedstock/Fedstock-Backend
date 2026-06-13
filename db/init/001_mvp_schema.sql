-- =========================================
-- PostgreSQL MVP Schema
-- Service: Authenticated AI Proxy
-- =========================================

-- 개발 초기화용입니다. 운영 DB에서는 직접 실행하지 않습니다.
DROP TABLE IF EXISTS ai_cluster_assignment_feature_importance CASCADE;
DROP TABLE IF EXISTS ai_cluster_assignment_feature_names CASCADE;
DROP TABLE IF EXISTS ai_cluster_assignment_queue CASCADE;
DROP TABLE IF EXISTS ai_fl_model_queue CASCADE;
DROP TABLE IF EXISTS ai_sync_rounds CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    store_id VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN'))
);

COMMENT ON TABLE users IS 'JWT 인증 사용자 계정';
COMMENT ON COLUMN users.email IS '로그인 이메일';
COMMENT ON COLUMN users.username IS '로그인 사용자명. 현재 프론트에서는 email과 같은 값으로 전송 가능';
COMMENT ON COLUMN users.store_id IS '클라이언트 또는 매장 식별자';
COMMENT ON COLUMN users.password_hash IS 'BCrypt 비밀번호 해시';
COMMENT ON COLUMN users.name IS '사용자 이름 또는 매장명';
COMMENT ON COLUMN users.role IS '서비스 권한: USER, ADMIN';

CREATE TABLE ai_sync_rounds (
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

CREATE TABLE ai_cluster_assignment_queue (
    id BIGSERIAL PRIMARY KEY,
    round_id VARCHAR(120) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    sample_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_ai_cluster_assignment_round_client
        UNIQUE (round_id, client_id)
);

CREATE TABLE ai_cluster_assignment_feature_names (
    queue_id BIGINT NOT NULL REFERENCES ai_cluster_assignment_queue(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL,
    feature_name VARCHAR(120) NOT NULL,
    PRIMARY KEY (queue_id, sort_order)
);

CREATE TABLE ai_cluster_assignment_feature_importance (
    queue_id BIGINT NOT NULL REFERENCES ai_cluster_assignment_queue(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL,
    feature_importance NUMERIC(18, 10) NOT NULL,
    PRIMARY KEY (queue_id, sort_order)
);

CREATE TABLE ai_fl_model_queue (
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

COMMENT ON TABLE ai_sync_rounds IS 'all_clients scope 요청을 라운드 단위로 모으는 상태 테이블';
COMMENT ON TABLE ai_cluster_assignment_queue IS '클러스터링 배정 all_clients 요청 큐';
COMMENT ON TABLE ai_fl_model_queue IS 'FL 모델 동기화 all_clients 요청 큐';

INSERT INTO users (email, username, store_id, password_hash, name, role)
VALUES
(
    'owner@example.com',
    'owner@example.com',
    'CA_1_FOODS_3',
    '$2a$10$HHu/aptIiVETMco09rL65.d69Ab6BC2tLO6QgExUuQCU5Oa7OztKy',
    'CA_1_FOODS_3',
    'USER'
),
(
    'client-4@example.com',
    'client-4@example.com',
    'CA_1_FOODS_4',
    '$2a$10$HHu/aptIiVETMco09rL65.d69Ab6BC2tLO6QgExUuQCU5Oa7OztKy',
    'CA_1_FOODS_4',
    'USER'
),
(
    'client-5@example.com',
    'client-5@example.com',
    'CA_1_FOODS_5',
    '$2a$10$HHu/aptIiVETMco09rL65.d69Ab6BC2tLO6QgExUuQCU5Oa7OztKy',
    'CA_1_FOODS_5',
    'USER'
);
