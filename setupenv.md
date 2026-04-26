SPRING_PROFILES_ACTIVE=prod
SPRING_APPLICATION_NAME=back-end-napolitech

SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0

DATASOURCE_URL=jdbc:mysql://db:3306/pizzaria_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
DATASOURCE_USERNAME=napolitech
DATASOURCE_PASSWORD=napolitech_dev
DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver

JWT_SECRET=e7c96235d3f2fdf3dc5cbf3033e4dd2321a80eb16699101cafc39276d20e2f55
JWT_EXPIRATION=86400000

FRONTEND_ALLOWED_ORIGINS=https://bonaripizzaria.netlify.app,https://internation-sully-kolten.ngrok-free.dev,http://localhost:3000,http://localhost:8081,http://localhost:19000,http://localhost:19006,http://10.0.2.2:8081

FRONTEND_BASE_URL=http://localhost:5173

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=mailsendernoreplyv8@gmail.com
MAIL_PASSWORD=gycihyclbdscmhor
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS=napolitech
BROKER_EXCHANGE_NAME=napolitech-exchange
BROKER_QUEUE_NAME=pedidos-queue
BROKER_ROUTING_KEY=pedidos-routing-key

REDIS_HOST=redis
REDIS_PORT=6379

OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_MODEL=llama3.2

NGROK_AUTHTOKEN=31LeTTBVo1nSF7PJ4fYc5xXK9Tf_6L5s8V2xVWsiSi3T8qRE8
