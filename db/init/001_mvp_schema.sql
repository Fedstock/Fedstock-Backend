-- =========================================
-- PostgreSQL MVP Schema
-- Service: Store-based Inventory Prediction
-- =========================================

-- 기존 테이블 삭제: 개발 초기화용
-- 운영 DB에서는 사용 주의
DROP TABLE IF EXISTS inventory_predictions CASCADE;
DROP TABLE IF EXISTS sales CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS store_members CASCADE;
DROP TABLE IF EXISTS stores CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =========================================
-- 1. users: 서비스 사용자
-- =========================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE users IS '서비스 사용자 계정';
COMMENT ON COLUMN users.email IS '로그인 이메일';
COMMENT ON COLUMN users.password_hash IS '암호화된 비밀번호';
COMMENT ON COLUMN users.name IS '사용자 이름';

-- =========================================
-- 2. stores: 매장
-- =========================================
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    business_type VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE stores IS '소상공인 매장 정보';
COMMENT ON COLUMN stores.name IS '매장명';
COMMENT ON COLUMN stores.business_type IS '업종 예: 카페, 식당, 편의점';

-- =========================================
-- 3. store_members: 사용자-매장 연결
-- =========================================
CREATE TABLE store_members (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'OWNER',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_store_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_store_members_store
        FOREIGN KEY (store_id)
        REFERENCES stores(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_store_members_user_store
        UNIQUE (user_id, store_id),

    CONSTRAINT chk_store_members_role
        CHECK (role IN ('OWNER', 'STAFF'))
);

COMMENT ON TABLE store_members IS '사용자와 매장을 연결하는 멤버 테이블';
COMMENT ON COLUMN store_members.role IS '매장 내 권한: OWNER, STAFF';

-- =========================================
-- 4. products: 상품
-- =========================================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    unit VARCHAR(30) NOT NULL DEFAULT 'EA',
    safety_stock INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_products_store
        FOREIGN KEY (store_id)
        REFERENCES stores(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_products_safety_stock
        CHECK (safety_stock >= 0)
);

COMMENT ON TABLE products IS '매장별 상품 정보';
COMMENT ON COLUMN products.name IS '상품명';
COMMENT ON COLUMN products.category IS '상품 카테고리';
COMMENT ON COLUMN products.unit IS '상품 단위 예: EA, BOX, KG';
COMMENT ON COLUMN products.safety_stock IS '안전 재고 수량';
COMMENT ON COLUMN products.is_active IS '상품 사용 여부';

-- =========================================
-- 5. inventory: 현재 재고
-- =========================================
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,

    quantity INTEGER NOT NULL DEFAULT 0,

    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_inventory_quantity
        CHECK (quantity >= 0)
);

COMMENT ON TABLE inventory IS '상품별 현재 재고';
COMMENT ON COLUMN inventory.product_id IS '상품 ID';
COMMENT ON COLUMN inventory.quantity IS '현재 재고 수량';

-- =========================================
-- 6. sales: 판매 기록
-- =========================================
CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,

    sold_quantity INTEGER NOT NULL,
    sold_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_sales_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_sales_sold_quantity
        CHECK (sold_quantity > 0)
);

COMMENT ON TABLE sales IS '상품별 판매 기록';
COMMENT ON COLUMN sales.product_id IS '판매된 상품 ID';
COMMENT ON COLUMN sales.sold_quantity IS '판매 수량';
COMMENT ON COLUMN sales.sold_at IS '판매 시점';

-- =========================================
-- 7. inventory_predictions: 재고 예측 결과
-- =========================================
CREATE TABLE inventory_predictions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,

    predicted_daily_sales NUMERIC(10, 2) NOT NULL,
    current_quantity INTEGER NOT NULL,
    expected_stockout_date DATE,
    recommendation_quantity INTEGER NOT NULL DEFAULT 0,

    predicted_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_inventory_predictions_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_predictions_predicted_daily_sales
        CHECK (predicted_daily_sales >= 0),

    CONSTRAINT chk_predictions_current_quantity
        CHECK (current_quantity >= 0),

    CONSTRAINT chk_predictions_recommendation_quantity
        CHECK (recommendation_quantity >= 0)
);

COMMENT ON TABLE inventory_predictions IS '상품별 재고 예측 결과';
COMMENT ON COLUMN inventory_predictions.predicted_daily_sales IS '하루 예상 판매량';
COMMENT ON COLUMN inventory_predictions.current_quantity IS '예측 시점의 현재 재고';
COMMENT ON COLUMN inventory_predictions.expected_stockout_date IS '예상 품절일';
COMMENT ON COLUMN inventory_predictions.recommendation_quantity IS '추천 발주 수량';
COMMENT ON COLUMN inventory_predictions.predicted_at IS '예측 실행 시점';

-- =========================================
-- Indexes
-- =========================================

CREATE INDEX idx_store_members_user_id
ON store_members(user_id);

CREATE INDEX idx_store_members_store_id
ON store_members(store_id);

CREATE INDEX idx_products_store_id
ON products(store_id);

CREATE INDEX idx_sales_product_id_sold_at
ON sales(product_id, sold_at);

CREATE INDEX idx_inventory_predictions_product_id_predicted_at
ON inventory_predictions(product_id, predicted_at DESC);

-- =========================================
-- Sample Data: 테스트용
-- 필요 없으면 아래 INSERT 구문은 제거 가능
-- =========================================

INSERT INTO users (email, password_hash, name)
VALUES
('owner@example.com', 'hashed_password_example', '테스트 사장님');

INSERT INTO stores (name, business_type)
VALUES
('재현 카페', '카페');

INSERT INTO store_members (user_id, store_id, role)
VALUES
(1, 1, 'OWNER');

INSERT INTO products (store_id, name, category, unit, safety_stock)
VALUES
(1, '아메리카노 원두', '원재료', 'KG', 3),
(1, '우유', '원재료', 'L', 5),
(1, '종이컵', '소모품', 'EA', 100);

INSERT INTO inventory (product_id, quantity)
VALUES
(1, 10),
(2, 20),
(3, 300);

INSERT INTO sales (product_id, sold_quantity, sold_at)
VALUES
(1, 2, NOW() - INTERVAL '3 days'),
(1, 1, NOW() - INTERVAL '2 days'),
(1, 3, NOW() - INTERVAL '1 day'),
(2, 5, NOW() - INTERVAL '2 days'),
(2, 4, NOW() - INTERVAL '1 day'),
(3, 30, NOW() - INTERVAL '1 day');

INSERT INTO inventory_predictions (
    product_id,
    predicted_daily_sales,
    current_quantity,
    expected_stockout_date,
    recommendation_quantity
)
VALUES
(1, 2.00, 10, CURRENT_DATE + 5, 5),
(2, 4.50, 20, CURRENT_DATE + 4, 10),
(3, 30.00, 300, CURRENT_DATE + 10, 200);
