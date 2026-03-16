## IDOR FIX
In getReviewById() implemented a check to compare user ID and review ID. If there is a mismatch, throw a 403 Forbidden response.

## DICTIONARY ATTACK FIX: ACCOUNT LOCKED FOR 5 MINUTES AFTER 3 FAILED ATTEMPTS
Modified authentication. Checks if user is in a lockout period. If so, dont allow login (timebased expiry). Implemented variables to track number of failed attempts. Resets the counter variable of failed attempts on successful login. 

## BUILD

```
sudo docker ps -a
sudo docker start [CONTAINER ID]

sudo fuser -k 9000/tcp
git clone https://github.com/mikiriki-m/websec-api-zadatak2.git
cd websec-api-zadatak2
git checkout vuln/idor
./mvnw clean package -DskipTests
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9000```

http://localhost:9000/login.html
