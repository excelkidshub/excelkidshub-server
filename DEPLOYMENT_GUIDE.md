# Production Deployment Guide - V9 Migration

## Overview
This deployment adds BaseEntity columns (active, created_by, updated_by) to the activity_progress table to fix schema validation errors.

## Pre-deployment Checklist
- [ ] Backup production database
- [ ] Review V9 migration SQL
- [ ] Test migration on staging environment (if available)

## Step 1: Deploy V9 Migration to Production Database

### Option A: Run Migration via Neon Dashboard
1. Go to Neon Dashboard → SQL Editor
2. Run the following SQL:
```sql
-- Add BaseEntity columns to activity_progress table
ALTER TABLE activity_progress 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
```

### Option B: Run Migration via psql CLI
```bash
psql -h ep-tiny-art-azfrqmrw-pooler.c-3.ap-southeast-1.aws.neon.tech -U neondb_owner -d excelkidshub -f src/main/resources/db/migration/V9__add_baseentity_columns.sql
```

### Option C: Let Flyway Run It (Recommended)
If you deploy the new JAR with Flyway enabled, it will automatically run V9 on startup.

## Step 2: Build and Deploy New JAR

### Build the JAR
```bash
cd D:\Git_ExcelKidsHub\excelkidshub-server
mvn clean package -DskipTests
```

### Deploy to Production
1. Copy `target/excelkidshub-platform-1.0.0.jar` to server
2. Stop existing service:
```bash
sudo systemctl stop excelkidshub
```

3. Replace JAR file:
```bash
sudo cp excelkidshub-platform-1.0.0.jar /opt/excelkidshub/
```

4. Start service:
```bash
sudo systemctl start excelkidshub
```

5. Check logs:
```bash
sudo journalctl -u excelkidshub -f
```

## Step 3: Verify Deployment

### Test API Endpoints
```bash
# Login
curl -X POST https://api.excelkidshub.in/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"praveend06@gmail.com","password":"test@123"}'

# Get progress summary
curl -X GET https://api.excelkidshub.in/api/progress/summary \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get activity summary
curl -X GET https://api.excelkidshub.in/api/progress/activities/summary \
  -H "Authorization: Bearer YOUR_TOKEN"

# Save activity progress
curl -X POST https://api.excelkidshub.in/api/progress/activity \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"courseId":2,"activityType":"PRACTICE","activityId":"sound-match-group-1","score":85,"completed":true}'
```

## Rollback Plan
If deployment fails:
1. Stop service: `sudo systemctl stop excelkidshub`
2. Restore previous JAR from backup
3. Start service: `sudo systemctl start excelkidshub`
4. If migration was applied, run rollback SQL:
```sql
ALTER TABLE activity_progress DROP COLUMN IF EXISTS created_by;
ALTER TABLE activity_progress DROP COLUMN IF EXISTS updated_by;
ALTER TABLE activity_progress DROP COLUMN IF EXISTS active;
```

## Post-deployment
- [ ] Verify API health: `curl https://api.excelkidshub.in/actuator/health`
- [ ] Check application logs for errors
- [ ] Test activity progress endpoints
- [ ] Monitor database performance
