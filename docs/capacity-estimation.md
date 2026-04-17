# Capacity Estimation

Estimate system scale (traffic, storage, bandwidth) to guide architectural decisions.

---

## Assumptions

- Daily active users (DAU): 10,000
- Actions per user per day: 5 (quote requests, tracking, etc.)
- Average data size per record: 1 KB
- Data retention period: 5 years

---

## Traffic Estimation

### Writes/day:
```
10,000 users x 5 actions
= 50,000 writes/day
```

### Reads/day:
```
Assume tracking + retries -> ~5x writes
= 250,000 reads/day
```

---

## QPS Calculation

There are 86,400 seconds in a day
```
Write QPS:
50,000 / 86,400 ≈ 0.6 ≈ 1 write/sec

Read QPS:
250,000 / 86,400 ≈ 2.9 ≈ 3 reads/sec
```

---

## Read/Write Ratio
```
~5:1 (moderately read-heavy)
```

---

## Storage Estimation
```
Daily storage:
50,000 × 1 KB = 50 MB/day

Yearly storage:
50 MB × 365 ≈ 18 GB/year

5 years:
≈ 90 GB
```

---

## Peak Traffic
```
Peak QPS (5x average):

Write: 5 req/sec
Read: 15 req/sec
```

---

## Key Observations

### 1. Not Huge Scale
- A single relational DB (Postgres/MySQL) is enough

### 2. Slightly Ready-Heavy
- Adding caching for:
    - Quote retrieval
    - Tracking endpoint

### 3. Low QPS BUT Critical Accuracy
- Focus more on:
    - data consistency
    - transactions
    - idempotency
      👉 This is a fintech system, not a social media app

### 4. Storage is Manageable
- No need for:
    - Sharding
    - Distributed DB (yet)

### 5. Peak Spikes Matter
- Design APIs to handle bursts
- Use:
    - Connection pooling
    - Efficient queries

