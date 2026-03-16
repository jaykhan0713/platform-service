- Update checkstyle on Sonar Cloud to detect import ordering so project imports are at bottom of groups.
- Migrate outbound to GRPC, proto DTOs.
  1) For java, can publish DTOs as lib
  2) In future for non java, must publish .proto and use protoc (code gen)
  3) For spring grpc lib, uses netty event loop. Make sure to have virtual thread metrics in grafana to check signs of pinning
- Switch to ZGC if increasing 0.5 vcpu to 1 vcpu