# OpenRxLight

A Quarkus-based backend application providing AI-powered voice interaction, account management, and notification services. Built with Java 17+ and Maven.

## Project Structure

```text
openrxlight/
├── openrxlight-sdk/      # SDK library wrapping OpenRxLight APIs
├── openrxlight-common/   # Common utilities shared across the project
├── rxlight/              # Main application (Quarkus API server)
└── pom.xml               # Parent POM
```

### Modules

| Module                 | Description                                                                                                                                                                                                                                                                                            |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **openrxlight-sdk**    | Client SDK providing HTTP/API wrappers for OpenRxLight services. Includes token management (`GlobalTokenManager`), data crypto utilities, digital signature, SSE streaming support, error/response models, and HTTP transport utilities. Publishable to Maven Central with GPG signing.                |
| **openrxlight-common** | Shared utilities and domain logic used by `rxlight` and other consumers. Provides Redis caching, token interceptors & stores, notification service, ORM extensions, AI audio/translation abstractions, event bus publisher/listener, and common domain models.            |
| **rxlight**            | The main Quarkus application. Exposes REST/WebSocket APIs, integrates with PostgreSQL (Hibernate ORM + Flyway), Redis, Local event bus, WeChat Mini Program, Lark bot, Alicloud TTS/translator, and S3-compatible file storage.                                              |

## Environment Preparation

The application requires the following infrastructure services running locally (for development) or in production.

### Prerequisites

- **JDK 21+** (We need Virtual Thread Feature)
- **Maven 3.9+** (or use the bundled `mvnw` wrapper)

### Redis

Used for token storage, caching, and real-time communication via Redis Pub/Sub.

```bash
# Docker
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Verify
redis-cli ping  # PONG
```

The dev profile connects to `redis://localhost:6379/0` with RESP3 protocol.

### PostgreSQL

Primary relational database for persistent data (accounts, configurations, migrations via Flyway).

```bash
# Docker
docker run -d --name postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=rxlight \
  -p 5432:5432 \
  postgres:16-alpine

# Verify
psql -h localhost -U postgres -d rxlight -c "SELECT 1"
```

Flyway migrations are located at `rxlight/src/main/resources/db/migration/` and run automatically at startup (`quarkus.flyway.migrate-at-start=true`).

## Configuration

The application reads configuration from environment variables. For local development, create a `.env` file in the `rxlight/` directory (see `.env.development` as a template):

```bash
# Database
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres

# Redis (auto-connected in dev mode to localhost:6379)

# OpenRxLight API
OPENRXLIGHT_API_CLIENTID=
OPENRXLIGHT_API_CLIENTSECRET=
OPENRXLIGHT_API_SIGNATURE_PRI_KEY=
OPENRXLIGHT_API_SIGNATURE_PUB_KEY=

# WeChat Mini Program
WECHAT_MINI_PROGRAM_APPID=
WECHAT_MINI_PROGRAM_APPSECRET=
WECHAT_PAY_APIV3KEY=
WECHAT_MINI_PROGRAM_SERVICE_ID=

# File Storage (S3-compatible, e.g. Alibaba OSS)
FS_BUCKET_NAME=
FS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
FS_REGION=cn-hangzhou
ALIBABA_CLOUD_ACCESS_KEY_ID=
ALIBABA_CLOUD_ACCESS_KEY_SECRET=

# LLM (DashScope / OpenAI-compatible)
LLM_API_KEY=
LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1

# Lark App
LARK_APP_ID=
LARK_APP_SECRET=
LARK_APP_ENCRYPT_KEY=
LARK_APP_VERIFICATION_TOKEN=

# Alicloud TTS
OPENAI_API_KEY=
ALICLOUD_TTS_VOICE=

# Admin
ADMIN_EMAIL=
```

## How to Start the Application

### Development Mode (VS Code)

The project provides VS Code launch configurations for one-click development:

1. Open the project root (`openrxlight/`) in VS Code
2. Go to **Run and Debug** (`Cmd+Shift+D` / `Ctrl+Shift+D`)
3. Select **RxLight-Api** and press **F5**

This triggers the `quarkus:dev` task which:

1. Installs all module dependencies (`mvn install`)
2. Cleans the `rxlight` workspace
3. Starts Quarkus in dev mode with hot-reload on `http://localhost:8000`
4. Attaches the Java debugger on port `5005`

### Development Mode (Terminal)

```bash
cd openrxlight/

# Install SDK and common modules first
./mvnw clean install -pl openrxlight-sdk,openrxlight-common -DskipTests

# Start the main application in dev mode
export JWT_SECRET='jwt_secret'
./mvnw quarkus:dev -pl rxlight
```

The application will be available at **<http://localhost:8000>** with Quarkus Dev UI at **<http://localhost:8000/q/dev>**.

### Native Build

```bash
./mvnw package -Pnative -pl rxlight
```

Requires GraalVM installed. The native executable will be generated under `rxlight/target/`.

## Alicloud TTS Voice Requirements

The application supports Alibaba Cloud TTS (Text-to-Speech) for AI voice output. Three credentials are required:

| Credential | Environment Variable | Description |
|---|---|---|
| **AccessKey ID** | `ALIBABA_CLOUD_ACCESS_KEY_ID` | Alibaba Cloud RAM user AccessKey ID. Used for Alicloud translator and S3 file storage. |
| **AccessKey Secret** | `ALIBABA_CLOUD_ACCESS_KEY_SECRET` | Alibaba Cloud RAM user AccessKey Secret. Paired with AccessKey ID. |
| **OpenAI-compatible API Key** | `OPENAI_API_KEY` | API Key for the OpenAI-compatible endpoint (e.g. DashScope `dashscope.aliyuncs.com/compatible-mode/v1`). Used by the TTS audio service. |

Additionally, configure the TTS voice name:

| Config | Environment Variable | Description |
|---|---|---|
| **TTS Voice** | `ALICLOUD_TTS_VOICE` | The voice model name to use for speech synthesis (e.g. `longxiaochun`, `aida`, etc.) |

These are set in the application configuration as:

```properties
openrxlight.ai.audio.type=alicloud
openrxlight.ai.audio.alicloud.api-key=${OPENAI_API_KEY}
openrxlight.ai.audio.alicloud.tts-voice=${ALICLOUD_TTS_VOICE}
```

The translator service also uses the Alibaba Cloud AccessKey credentials:

```properties
openrxlight.ai.translator.type=alicloud
openrxlight.ai.translator.alicloud.access-key=${ALICLOUD_ACCESS_KEY_ID}
openrxlight.ai.translator.alicloud.secret-key=${ALICLOUD_ACCESS_KEY_SECRET}
```

## Tech Stack

- **Framework**: Quarkus 3.30.4
- **Language**: Java 21+
- **Database**: PostgreSQL + Hibernate ORM 7.x + Flyway
- **Cache**: Redis (Lettuce client, RESP3 protocol)
- **Build**: Maven 3.9+ with Maven Wrapper
- **CI/CD**: GitHub Actions
- **Container**: Docker (Alibaba Cloud ACR)
- **Other**: gRPC, Protobuf, WeChat SDK, Lark SDK, S3 (AWS SDK v2)
