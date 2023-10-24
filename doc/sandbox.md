```bash
scp -i /Users/hemiao/pem/me2022.cer /Users/hemiao/ocpp/steve/docker-compose.yml root@47.242.32.120:/root/steve/

docker pull joehe/ocpp-steve-server:3.6.06

docker-compose up -d

docker-compose stop
docker-compose rm -f
```