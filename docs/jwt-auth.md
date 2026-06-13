# JWT Authentication

This project uses stateless JWT authentication for `/api/**`.

## Public APIs

```http
POST /api/auth/signup
POST /api/auth/login
```

Protected API routes require `Authorization: Bearer <accessToken>`.

```http
GET /api/users/me
POST /api/auth/logout
POST /api/ai/clients/cluster-assignment
POST /api/ai/clients/{clientId}/fl-model
```

## Local Secret

`src/main/resources/application.yml` contains a development fallback secret:

```yaml
jwt:
  secret: ${JWT_SECRET:dev-temp-secret-key-only-for-local-development-please-change-this-1234567890}
  access-token-expiration-ms: 3600000
```

For production, set `JWT_SECRET` in the runtime environment. Do not use the fallback secret in production.
The `prod` profile requires `JWT_SECRET` and does not inherit the local fallback.

## Local Test

Create a user:

```bash
curl -i -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","storeId":"CA_1_FOODS_3","username":"test@example.com","password":"password12","name":"CA_1_FOODS_3"}'
```

Login:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password12"}'
```

Call a protected API:

```bash
TOKEN="paste-access-token-here"

curl -i http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

Verify missing token returns 401:

```bash
curl -i http://localhost:8080/api/users/me
```

Logout:

```bash
curl -i -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

## Sample DB User

`db/init/001_mvp_schema.sql` seeds three demo clients:

```text
emails: owner@example.com, client-4@example.com, client-5@example.com
storeIds: CA_1_FOODS_3, CA_1_FOODS_4, CA_1_FOODS_5
password: password12
role: USER
```

Existing databases created before this change need a migration similar to:

```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS store_id VARCHAR(100);
UPDATE users SET username = email WHERE username IS NULL;
UPDATE users SET store_id = name WHERE store_id IS NULL;
ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ALTER COLUMN store_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_username_key UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT users_store_id_key UNIQUE (store_id);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(30) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));
```

The `all_clients` AI proxy flow also requires the queue tables from
`/Users/kento/Desktop/Project/fedstock/Fedstock-Backend/db/init/001_mvp_schema.sql`:

```sql
ai_sync_rounds
ai_cluster_assignment_queue
ai_cluster_assignment_feature_names
ai_cluster_assignment_feature_importance
ai_fl_model_queue
```
