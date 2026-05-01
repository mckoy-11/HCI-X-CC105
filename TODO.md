# Barangay Window Module Implementation TODO

## Phase 1: Model Updates
- [ ] 1.1 Update Account model - Add isBarangaySetupComplete field
- [ ] 1.2 Update AccountDao - Handle new field in CRUD operations
- [ ] 1.3 Update Barangay model - Add purokCount and population fields
- [ ] 1.4 Update BarangayDao - Bind and retrieve new fields

## Phase 2: UI Components
- [ ] 2.1 Create BarangaySetupDialog - Modal dialog for onboarding
- [ ] 2.2 Create reusable confirmation checkbox component

## Phase 3: Integration
- [ ] 3.1 Modify LoginPanel - Check setup status on login
- [ ] 3.2 Modify MainFrame - Handle barangay setup dialog
- [ ] 3.3 Update ScheduleFormDialog - Uses barangay names from DB

## Phase 4: Testing & Verification
- [ ] 4.1 Compile and test the changes
- [ ] 4.2 Verify integration flows
