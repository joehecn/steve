```bash
docker build -t joehe/ocpp-steve-server:3.6.06 .

docker buildx build --platform linux/arm64,linux/amd64 -t joehe/ocpp-steve-server:3.6.06 --push .
```