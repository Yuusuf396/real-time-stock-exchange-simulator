# Real-Time Stock Exchange Simulator

## Goal
Build a realistic exchange simulator that demonstrates distributed systems,
high-performance backend engineering, and modern full-stack architecture.

## Tech Stack
- Java: Matching engine
- Spring Boot: API gateway, WebSockets, auth
- Kafka: Event bus
- PostgreSQL + TimescaleDB
- Redis
- Next.js
- Docker Compose

## Repository Structure

exchange/
├── docs/
├── engine/
├── gateway/
├── dashboard/
├── infra/
├── README.md
└── CLAUDE.md

## Build Order

Phase 1
- Matching engine

Phase 2
- Kafka integration

Phase 3
- Spring Boot gateway

Phase 4
- Persistence

Phase 5
- Dashboard

## Rules

- One feature at a time
- Explain before coding
- Review after every implementation
- Never modify unrelated folders