- Update checkstyle on Sonar Cloud to detect import ordering so project imports are at bottom of groups.
- Migrate outbound to GRPC, proto DTOs.
  1) For java, can publish DTOs as lib
  2) In future for non java, must publish .proto and use protoc (code gen)
  3) For spring grpc lib, uses netty event loop. Make sure to have virtual thread metrics in grafana to check signs of pinning
- Potentially switch to ZGC if increasing 0.5 vcpu to 1 vcpu. ZGC uses more heap memory so need to increase from 512. (no RSS uncommit)
- for production aimed, logstash-logback-encoder → JSON stdout → Filebeat → Logstash → Elasticsearch
- remove manual MDC context propagation in favor of micrometer ContextExecutorService (refer to edge-service)
- Envoy access logs for real failures
- add CB automatic state transition to HALF_OPEN in services.
- Add edge service Spring Security + handling JWT + OAuth2 for phase outside of AWS not relying on api gateway.