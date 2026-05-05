.PHONY: dev build test clean

dev:
	@echo "PostgreSQL 상태 확인 중..."
	@if [ "$$(docker inspect -f '{{.State.Running}}' waytofit-postgres 2>/dev/null)" = "true" ]; then \
		echo "PostgreSQL 이미 실행 중. 빌드를 시작합니다."; \
	else \
		echo "PostgreSQL 미실행. docker-compose up -d 실행 중..."; \
		docker-compose up -d; \
		echo "DB 준비 대기 중..."; \
		until [ "$$(docker inspect -f '{{.State.Health.Status}}' waytofit-postgres 2>/dev/null)" = "healthy" ]; do \
			sleep 2; \
			echo "  대기 중..."; \
		done; \
		echo "PostgreSQL 준비 완료."; \
	fi
	./gradlew bootRun

build:
	./gradlew build

test:
	./gradlew test

clean:
	./gradlew clean
