# Help and Troubleshooting

This document contains common setup, runtime, and debugging guidance for this service.

---

## Running locally

### Docker
If the service fails to start using Docker:

- Ensure the Docker daemon is running
- If using Docker CLI only, Docker Desktop must be running

Run with:
```
docker network create platform

docker compose up --build
```

---

### Gradle
If running via Gradle:

- Ensure Java 25 is installed 

Use an explicit profile:
```
./gradlew bootRun --args="--spring.profiles.active=dev"
```

---

## Configuration and profiles

- The service relies on Spring profiles for environment-specific configuration
- Local development typically uses the `dev` profile
- Production configuration is expected to be supplied via environment variables

---

## Observability and debugging

### Logs
- Logs are structured and include trace IDs
- MDC values such as requestId and userId are populated at the inbound/web boundary

### Health endpoints
- /actuator/health
- /actuator/info

If health endpoints are unavailable, verify actuator exposure settings.

---

## Common issues

### Port already in use
If startup fails due to a port conflict:
- Verify no other service is running on the configured port
- Adjust the server port via environment variables if needed

### Missing configuration
If the application fails fast during startup:
- Check logs for missing configuration keys
- Verify the active Spring profile

---

## Known limitations

- Authentication and authorization are expected to be handled at the edge. A user sub derived from cognito JWT will be propagated by API gateway in request headers as x-user-id, always.
