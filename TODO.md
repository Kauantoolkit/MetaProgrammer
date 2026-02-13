# Admin User Password Fix - Implementation Plan

## Problem
The admin user migration creates a user with a hardcoded BCrypt hash that doesn't match the expected password, causing login failures.

## Solution
Generate a random secure password at generation time, store it in application.properties, and create the admin user on startup using a proper BCrypt hash.

## Tasks

- [x] 1. Modify FlywayMigrationGenerator.java - Remove V2__Create_admin_user.sql generation
- [x] 2. Create AdminUserInitializerGenerator.java - Generate component to create admin on startup
- [x] 3. Modify ApplicationPropertiesGenerator.java - Add admin.password property
- [x] 4. Modify CodeGeneratorService.java - Generate random password and log it
- [ ] 5. Test the implementation


## Implementation Details

### Step 1: Remove V2 Migration
Remove the code that generates V2__Create_admin_user.sql from FlywayMigrationGenerator.java

### Step 2: Create AdminUserInitializerGenerator
Generate a component that:
- Runs on application startup using @EventListener(ApplicationReadyEvent.class)
- Checks if admin user exists
- Creates admin user with password from application.properties
- Uses PasswordEncoder to properly hash the password

### Step 3: Update ApplicationPropertiesGenerator
Add admin.password property with the generated random password

### Step 4: Update CodeGeneratorService
- Generate random secure password (16 characters, alphanumeric + special)
- Pass password to generators
- Log the password prominently after generation
