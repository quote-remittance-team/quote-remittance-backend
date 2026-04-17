# Quote-Based Remittance System

## Core Goal
Build a Quote-Based Money Remittance System that:
- Allows users to preview transfer details (quote)
- Locks the exchange rate for a short time
- Ensures safe, consistent, and reliable money transfer

### In Simple Terms
```
Let users see exactly what they'll send and what the receiver will get, and guarantee that amount when they proceed
```

---

## Functional Requirements
These describe what the system must do

---
### 1. Quote Management
- Request a quote
- Calculate:
    - Exchange rate
    - Fees
    - Receive amount
- Generate:
    - Unique reference
    - Expiry time
- Auto-expire quotes

---
### 2. Quote Validation
- Accept only:
    - ACTIVE quotes
- Reject:
    - EXPIRED quotes
    - USED quotes

---

### 3. Payment (Deposit)
- Create deposit after quote acceptance
- Confirm payment (via gateway/webhook)
- Store payment reference
- Handle failed payment

---

### 4. Remittance Processing
- Create remittance only after payment confirmation
- Use locked exchange rate
- Execute payout
- Mark completion

---

### 5. Tracking & Visibility
- Track remittance using reference
- Show:
    - Quote status
    - Deposit status
    - Remittance status

---

### 6. Error Handling
- Handle failures:
    - Payment failure
    - Transfer failure
- Retry or refund logic

---

### 7. Idempotency
- Prevent duplicate:
    - Payments
    - Request
- Ensure safe retries

---

### 8. Notifications
- Notify users on:
    - Payment confirmation
    - Transfer completion
    - Failure

---

## Non-Functional Requirements

These define how well the system should perform, this is where systems become "enterprise-grade".

---

### 1. Consistency
- Quote values must NOT change after creation
- Payment and remittance must be accurate (no money mismatch ever)

---

### 2. Reliability
- System must:
    - Handle failures gracefully
    - Avoid data loss
    - Recover from crashes

### 3. Idempotency
- Same request should not:
    - Charge user twice
    - Create duplicate remittance

---

### 4. Performance
- Quote generation should be fast (< 1s)
- Tracking should be near real-time

---

### 5. Scalability
- Should handle:
    - Many users requesting quotes
    - High read traffic (tracking)

---

### 6. Security
- Protect:
    - Payment data
    - User data
- Validate all inputs

---

### 7. Auditability
- Every transaction must be:
    - Logged
    - Traceable

Important for:
- Compliance
- Debugging
- Financial audits

---

### 8. Observability
- Logs
- Metrics
- Alerts

So you can detect:
- Failed payments
- System issues

---

### 9. Testability
- Unit tests
- Integration tests
- Mock external services

