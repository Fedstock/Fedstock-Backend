# Authenticated AI Proxy API Spec

Last checked: 2026-06-13

Spring exposes two authenticated client APIs. `single_client` requests are forwarded to AI immediately. `all_clients` requests are stored by `roundId + clientId`; when all registered `users.store_id` clients have submitted for the same round, Spring sends one batch request to AI.

## Common Rules

| 항목 | 내용 |
|---|---|
| Auth | `Authorization: Bearer <accessToken>` required |
| Client ID check | `clientId` or `client_id` must exist in `users.store_id` |
| all_clients expected count | Spring uses `SELECT COUNT(*) FROM users` |
| Duplicate client in same round | Upsert: latest payload replaces previous queued payload |
| Pending response | `202 Accepted` with queue status |
| Final response | Last client receives the AI response |

Seed demo clients:

```text
CA_1_FOODS_3
CA_1_FOODS_4
CA_1_FOODS_5
```

## 1. Initial Clustering Assignment

### Client to Spring

```http
POST /api/ai/clients/cluster-assignment
Authorization: Bearer <accessToken>
Content-Type: application/json
```

#### single_client request

```json
{
  "scope": "single_client",
  "roundId": "initial-clustering-20260613-001",
  "clientId": "CA_1_FOODS_3",
  "sampleCount": 13004,
  "featureNames": ["rolling_mean_28", "rolling_mean_7", "lag_7"],
  "featureImportance": [0.1421, 0.1318, 0.0974],
  "expectedClientCount": null
}
```

Spring forwards this immediately to AI:

```http
POST {AI_BACKEND_URL}/clients/cluster-assignment
Content-Type: application/json
```

#### all_clients request demo

Client 1:

```json
{
  "scope": "all_clients",
  "roundId": "initial-clustering-20260613-demo",
  "clientId": "CA_1_FOODS_3",
  "sampleCount": 13004,
  "featureNames": ["rolling_mean_28", "rolling_mean_7", "lag_7"],
  "featureImportance": [0.1421, 0.1318, 0.0974],
  "expectedClientCount": 3
}
```

Pending response:

```json
{
  "api": "cluster-assignment",
  "scope": "all_clients",
  "roundId": "initial-clustering-20260613-demo",
  "clientId": "CA_1_FOODS_3",
  "expectedClientCount": 3,
  "receivedClientCount": 1,
  "status": "QUEUED",
  "forwarded": false
}
```

When `CA_1_FOODS_3`, `CA_1_FOODS_4`, and `CA_1_FOODS_5` are all queued, Spring sends one AI request:

```http
POST {AI_BACKEND_URL}/clients/cluster-assignment/batch
Content-Type: application/json
```

```json
{
  "scope": "all_clients",
  "roundId": "initial-clustering-20260613-demo",
  "expectedClientCount": 3,
  "clients": [
    {
      "clientId": "CA_1_FOODS_3",
      "sampleCount": 13004,
      "featureNames": ["rolling_mean_28", "rolling_mean_7", "lag_7"],
      "featureImportance": [0.1421, 0.1318, 0.0974]
    },
    {
      "clientId": "CA_1_FOODS_4",
      "sampleCount": 9802,
      "featureNames": ["rolling_mean_28", "rolling_mean_7", "lag_7"],
      "featureImportance": [0.1204, 0.1102, 0.0881]
    },
    {
      "clientId": "CA_1_FOODS_5",
      "sampleCount": 7211,
      "featureNames": ["rolling_mean_28", "rolling_mean_7", "lag_7"],
      "featureImportance": [0.1322, 0.1015, 0.0764]
    }
  ]
}
```

## 2. FL Model Sync

### Client to Spring

```http
POST /api/ai/clients/{clientId}/fl-model
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

#### single_client request

```bash
curl -i -X POST http://localhost:8080/api/ai/clients/CA_1_FOODS_3/fl-model \
  -H "Authorization: Bearer $TOKEN" \
  -F "client_id=CA_1_FOODS_3" \
  -F "scope=single_client" \
  -F "round_id=fl-sync-20260613-001" \
  -F "sample_weight=13004" \
  -F "model_file=@client_CA_1_FOODS_3.pt"
```

Spring forwards this immediately to AI:

```http
POST {AI_BACKEND_URL}/clients/CA_1_FOODS_3/fl-model
Content-Type: multipart/form-data
```

#### all_clients request demo

```bash
curl -i -X POST http://localhost:8080/api/ai/clients/CA_1_FOODS_3/fl-model \
  -H "Authorization: Bearer $TOKEN" \
  -F "client_id=CA_1_FOODS_3" \
  -F "scope=all_clients" \
  -F "round_id=fl-sync-20260613-demo" \
  -F "sample_weight=13004" \
  -F "model_file=@client_CA_1_FOODS_3.pt"
```

Pending response:

```json
{
  "api": "fl-model",
  "scope": "all_clients",
  "roundId": "fl-sync-20260613-demo",
  "clientId": "CA_1_FOODS_3",
  "expectedClientCount": 3,
  "receivedClientCount": 1,
  "status": "QUEUED",
  "forwarded": false
}
```

When all clients are queued, Spring sends one AI request:

```http
POST {AI_BACKEND_URL}/clients/fl-model/batch
Content-Type: multipart/form-data
```

Multipart parts:

| Part | Type | Description |
|---|---|---|
| `metadata` | `application/json` | Round and per-client model metadata |
| `model_files` | repeated file parts | `.pt` model files ordered by `client_id` |

`metadata` demo:

```json
{
  "scope": "all_clients",
  "round_id": "fl-sync-20260613-demo",
  "expected_client_count": 3,
  "models": [
    {
      "client_id": "CA_1_FOODS_3",
      "sample_weight": 13004,
      "filename": "client_CA_1_FOODS_3.pt"
    },
    {
      "client_id": "CA_1_FOODS_4",
      "sample_weight": 9802,
      "filename": "client_CA_1_FOODS_4.pt"
    },
    {
      "client_id": "CA_1_FOODS_5",
      "sample_weight": 7211,
      "filename": "client_CA_1_FOODS_5.pt"
    }
  ]
}
```
