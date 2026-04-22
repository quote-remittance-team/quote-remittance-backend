<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Quote-Based Remittance System - High Level Design](#quote-based-remittance-system---high-level-design)
  - [Goal](#goal)
  - [High-Level Architecture Diagram](#high-level-architecture-diagram)
  - [Architecture Style](#architecture-style)
  - [Core Components](#core-components)
    - [1. Frontend (Client Layer)](#1-frontend-client-layer)
    - [2. Backend (Application Layer)](#2-backend-application-layer)
      - [Core Modules:](#core-modules)
    - [3. Database Layer](#3-database-layer)
    - [4. External Systems](#4-external-systems)
  - [Core Workflow](#core-workflow)
    - [1. Request Quote](#1-request-quote)
    - [2. Accept Quote](#2-accept-quote)
    - [3. Confirm Deposit](#3-confirm-deposit)
    - [4. Create Remittance](#4-create-remittance)
    - [5. Execute Payout](#5-execute-payout)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Quote-Based Remittance System - High Level Design
This document describes the high-level architecture of the **Quote-Based Money Remittance System**.

The system allows users to:
- Request a quote for currency exchange
- Accept the quote within a validity period
- Make a deposit
- Process remittance
- Execute payout to the receiver

---

## Goal

Define the main components of the system and how they interact

---

## High-Level Architecture Diagram
![Architecture Diagram]('https://github.com/quote-remittance-team/quote-remittance-backend/tree/main/docs/diagrams/quote-remittance-architecture.drawio.png')

## Architecture Style

We use a **Modular Monolith Architecture**.

**Why?**
- Simpler to build within the project timeline
- Easier to maintain and debug
- Clear separation of concerns
- Can evolve into microservices later

## Core Components

### 1. Frontend (Client Layer)
- Built with React
- Responsible for:
  - User interaction
  - Sending API requests
  - Displaying remittance status

---

### 2. Backend (Application Layer)
- Built with Java 21 + Spring Boot
- Exposes REST APIs
- Contains business logic

#### Core Modules:
- **Quote Module**
  - Generate exchange quotes
  - Calculate fees and rates
  - Set expiry time
- **Deposit Module**
  - Accept quote
  - Create deposit record
  - Track payment status
- **Remittance Module**
  - Create remittance after deposit confirmation
  - Manage remittance lifecycle
- **Payout Module**
  - Execute transfer to receiver
  - Mark remittance as completed
- **Notification Module**
  - Send updates (e.g., deposit confirmed, remittance completed)
- **Auth Module**
  - Users authenticate via login
  - A JWT token is issued
  - All protected endpoints require a valid token

---

### 3. Database Layer
- PostgreSQL
- Stores:
  - Users
  - Quotes
  - Remittances
  - Notifications
  - Transaction logs

---

### 4. External Systems
- Payment Gateway (for deposit confirmation)
- Payout Provider (for sending funds)

---

## Core Workflow

### 1. Request Quote
- User provides:
  - Send amount
  - Sender currency
  - Receiver currency

- System generates:
  - Exchange rate (Provider)
  - Fees
  - Receive amount
  - Expiry timestamp (Based exchange rate)

### 2. Accept Quote
- System validates:
  - Quote is ACTIVE
  - Not expired
  - Marks quote as USED
  - Creates deposit (PENDING)

### 3. Confirm Deposit
- Payment is verified
- Deposit status updated:
  - PENDING → CONFIRMED

### 4. Create Remittance
- Triggered after deposit confirmation
- Exchange rate is locked
- Status set to PROCESSING

### 5. Execute Payout
- Funds sent to receiver
- Status updated to COMPLETED


