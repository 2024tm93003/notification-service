# Notification Service

Spring Boot microservice that sends customer email notifications for banking events such as high-value transactions, account status changes, and important account updates (loan lifecycle, document uploads, bill payments, and contact information changes). The service is designed for a microservices environment with database-per-service boundaries and accepts event payloads over HTTP.

## Features
- RESTful endpoints to trigger notifications from upstream transaction/account services.
- Configurable high-value transaction thresholds with per-request overrides.
- Email delivery via SMTP or mock console logging (useful for local development).
- Spring Profiles & `.env` files to keep environment-specific settings isolated.
- Docker container for consistent deployment.
- Actuator health endpoint for basic observability.

## Project Layout

```
src/main/java/com/bank/notificationservice
├── controller      # REST controllers and exception handling
├── dto             # Request/response payloads
├── model           # Domain enums
├── service         # Notification composition & dispatch logic
├── support         # Shared helper records/exceptions
└── config          # Typed configuration properties
src/main/resources
├── application.yml         # Shared defaults
└── application-local.yml   # Local profile (mock delivery on)
env/
└── local.env               # Sample environment variables
```

## Configuration

Key properties live under the `notification` prefix:

| Property | Description |
| --- | --- |
| `notification.mail.from` | Default `from` address for outbound emails |
| `notification.mail.mock-delivery` | When `true`, emails are logged instead of sent |
| `notification.thresholds.high-value-transaction` | Default minimum amount that qualifies as "high value" |

### Profiles
- `local`: Default development profile. Logs email contents instead of sending. Configured via `application-local.yml`.

Activate a profile through the `SPRING_PROFILES_ACTIVE` environment variable (e.g., `local`).

### Environment Files
Use the supplied `.env` template with Docker or your process manager:

- `env/local.env`: Uses the `local` profile with mock delivery enabled.

> **Tip:** Adjust `notification.mail.mock-delivery` in the env file if you want to toggle between mock and real delivery without changing property files.

## Building

### With Docker (recommended)
```bash
docker build -t notification-service:local .
```

The multi-stage build compiles the Spring Boot application and produces a runnable container.

### With Maven (if installed locally)
```bash
mvn clean package
```

The runnable JAR is placed at `target/notification-service-0.1.0-SNAPSHOT.jar`.

## Running

### Docker
```bash
# Local/dev mode (mock delivery)
docker run --rm -p 8080:8080 --env-file env/local.env notification-service:local
```

### JVM
```bash
SPRING_PROFILES_ACTIVE=local java -jar target/notification-service-0.1.0-SNAPSHOT.jar
```

## API

Base path: `/api/notifications`

| Endpoint | Description |
| --- | --- |
| `POST /transactions/high-value` | Trigger high value transaction email |
| `POST /accounts/status-change` | Notify customers about status transitions |
| `POST /accounts/events` | Notify customers about account events (contact/documents/loan/bill changes) |

Supported `eventType` values:
- `ACCOUNT_NUMBER_UPDATED`
- `CONTACT_INFORMATION_UPDATED`
- `DOCUMENT_UPDATED`
- `LOAN_TAKEN`
- `LOAN_CLEARED`
- `BILL_CLEARED`

### Sample Requests

#### High-Value Transaction
```bash
curl -X POST http://localhost:8080/api/notifications/transactions/high-value \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "123-456-789",
    "customerName": "Jane Doe",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "txnType": "DEBIT",
    "amount": 15000,
    "currency": "INR",
    "counterparty": "Acme Stores",
    "reference": "Invoice 8821"
  }'
```

#### Account Status Change
```bash
curl -X POST http://localhost:8080/api/notifications/accounts/status-change \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "987-654-321",
    "customerName": "John Smith",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "previousStatus": "Pending KYC",
    "currentStatus": "Active",
    "remarks": "Documents verified"
  }'
```

#### Account Event Notifications
Below are sample payloads for each supported `eventType`.

```bash
# Account number updated
curl -X POST http://localhost:8080/api/notifications/accounts/events \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "555-666-777",
    "customerName": "Mary Major",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "eventType": "ACCOUNT_NUMBER_UPDATED",
    "description": "Your new account number is 555-666-999"
  }'

# Contact information updated
curl -X POST http://localhost:8080/api/notifications/accounts/events \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "555-666-777",
    "customerName": "Mary Major",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "eventType": "CONTACT_INFORMATION_UPDATED",
    "description": "Mobile number updated to +91-9000000000"
  }'

# Document updated
curl -X POST http://localhost:8080/api/notifications/accounts/events \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "555-666-777",
    "customerName": "Mary Major",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "eventType": "DOCUMENT_UPDATED",
    "description": "Latest bank statement uploaded"
  }'

# Loan taken
curl -X POST http://localhost:8080/api/notifications/accounts/events \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "555-666-777",
    "customerName": "Mary Major",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "eventType": "LOAN_TAKEN",
    "description": "Personal loan of INR 500,000 disbursed"
  }'

# Loan cleared
curl -X POST http://localhost:8080/api/notifications/accounts/events \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "555-666-777",
    "customerName": "Mary Major",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "eventType": "LOAN_CLEARED",
    "description": "Final EMI received on 2024-02-20"
  }'

# Bill cleared
curl -X POST http://localhost:8080/api/notifications/accounts/events \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "555-666-777",
    "customerName": "Mary Major",
    "customerEmail": "noreply2024tm93003@gmail.com",
    "eventType": "BILL_CLEARED",
    "description": "Electricity bill payment of INR 2,300 confirmed"
  }'
```

### Responses
- `202 Accepted` when the notification is queued for delivery.
- `200 OK` for high-value requests that fell below the configured threshold (email skipped).
- `400 Bad Request` when validation fails (field-level errors included).

## Testing

When Maven is available:
```bash
mvn test
```

The test suite currently validates the email template composition logic.

## Health Check
- `GET /actuator/health` returns service health.
- `GET /actuator/info` can be extended with build metadata if desired.

## Next Steps
- Integrate with a real SMTP server or email provider.
- Publish events to a message broker to decouple request handling from delivery.
- Add structured logging/observability hooks for production use.
