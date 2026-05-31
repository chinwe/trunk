# 构建阶段
FROM golang:1.25-alpine AS builder

WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app/demo ./cmd/demo

# 运行阶段
FROM alpine:latest

RUN apk --no-cache add ca-certificates
WORKDIR /app

COPY --from=builder /app/demo .
COPY configs/ ./configs/

EXPOSE 8000 9000

CMD ["./demo", "-conf", "./configs"]
