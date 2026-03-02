# BidBid 배포 가이드

## GitHub Actions Secrets 설정 목록

GitHub 저장소 → Settings → Secrets and variables → Actions 에서 아래 시크릿을 등록한다.

### AWS 관련
| Secret 이름 | 설명 |
|---|---|
| `AWS_ACCESS_KEY_ID` | IAM 사용자 Access Key (ECR Push + EC2 접근 권한 필요) |
| `AWS_SECRET_ACCESS_KEY` | IAM 사용자 Secret Key |
| `ECR_REGISTRY` | ECR 레지스트리 주소 (예: `123456789.dkr.ecr.ap-northeast-2.amazonaws.com`) |

### EC2 SSH 관련
| Secret 이름 | 설명 |
|---|---|
| `EC2_HOST` | EC2 인스턴스 퍼블릭 IP 또는 도메인 |
| `EC2_USER` | SSH 접속 유저명 (예: `ubuntu`, `ec2-user`) |
| `EC2_SSH_KEY` | EC2 접속용 PEM 키 내용 (-----BEGIN RSA PRIVATE KEY----- 포함) |

### 애플리케이션 관련
| Secret 이름 | 설명 |
|---|---|
| `NEXT_PUBLIC_API_URL` | 백엔드 API URL (예: `https://api.your-domain.com`) |
| `NEXT_PUBLIC_WS_URL` | WebSocket URL (예: `wss://api.your-domain.com`) |

### 알림 관련
| Secret 이름 | 설명 |
|---|---|
| `TELEGRAM_BOT_TOKEN` | 배포 알림용 텔레그램 봇 토큰 |
| `TELEGRAM_CHAT_ID` | 배포 알림 수신 Chat ID |

---

## EC2 초기 설정

```bash
# 1. Docker 설치
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu

# 2. AWS CLI 설치 및 구성
sudo apt install -y awscli
aws configure  # Access Key, Secret Key, Region 입력

# 3. 프로젝트 디렉토리 생성
mkdir -p ~/bidbid
cd ~/bidbid

# 4. .env 파일 생성 (docker-compose.prod.yml에서 참조)
cat > .env <<EOF
DB_URL=jdbc:postgresql://your-rds-endpoint:5432/auction
DB_USERNAME=auction
DB_PASSWORD=your-db-password
REDIS_HOST=your-elasticache-endpoint
REDIS_PORT=6379
JWT_SECRET=your-jwt-secret-at-least-32-chars
S3_BUCKET=your-s3-bucket-name
AWS_ACCESS_KEY=your-aws-access-key
AWS_SECRET_KEY=your-aws-secret-key
TELEGRAM_BOT_TOKEN=your-telegram-bot-token
POSTGRES_PASSWORD=your-postgres-password
GRAFANA_ADMIN_PASSWORD=your-grafana-password
ECR_REGISTRY=your-ecr-registry
IMAGE_TAG=latest
EOF

# 5. docker-compose.prod.yml 복사 후 실행
docker compose -f docker-compose.prod.yml up -d
```

## SSL 인증서 발급 (Let's Encrypt)

```bash
# Certbot 설치
sudo apt install -y certbot

# 인증서 발급 (도메인 소유 확인)
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# 자동 갱신 크론탭 등록 (90일마다 갱신)
echo "0 0 1 * * certbot renew --quiet && docker exec auction-nginx nginx -s reload" | sudo crontab -
```

## AWS 인프라 구성

```
EC2 t3.small       → 애플리케이션 서버 (백엔드 + 프론트엔드 + Nginx)
RDS PostgreSQL      → db.t3.micro (프리티어)
ElastiCache Redis   → cache.t3.micro
S3 버킷             → 경매 이미지 저장
ECR                → Docker 이미지 레지스트리
Route53 + ACM       → 도메인 + SSL 인증서
```

## ECR 레포지토리 생성

```bash
aws ecr create-repository --repository-name bidbid-backend --region ap-northeast-2
aws ecr create-repository --repository-name bidbid-frontend --region ap-northeast-2
```
