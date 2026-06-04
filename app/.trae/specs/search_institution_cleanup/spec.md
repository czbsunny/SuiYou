# Search Institution Page Cleanup - Product Requirement Document

## Overview
- **Summary**: Clean up the search-institution.vue page by removing mock default data and the "手动添加其他机构" (Manual Add Other Institution) functionality.
- **Purpose**: Simplify the page and remove unused/demo code.
- **Target Users**: App users searching for financial institutions.

## Goals
- Remove mock data fallback (`getMockInstitutions` function)
- Remove the manual add institution card and related functionality

## Non-Goals (Out of Scope)
- Changing the core search functionality
- Modifying other pages or components

## Background & Context
The search-institution page currently contains:
1. Mock data fallback for when API fails (`getMockInstitutions`)
2. A "手动添加其他机构" (Manual Add Other Institution) card that shows a "功能开发中" (Feature in development) toast

These features are not needed for production and should be removed.

## Functional Requirements
- **FR-1**: Remove the `getMockInstitutions` function and its usage
- **FR-2**: Remove the manual add institution card from the UI
- **FR-3**: Remove the `handleManualAdd` function

## Non-Functional Requirements
- **NFR-1**: Code should remain syntactically correct and build without errors
- **NFR-2**: Page should still function properly after changes

## Constraints
- **Technical**: UniApp/Vue 3 syntax must be maintained
- **Dependencies**: Must not break any other pages or components

## Assumptions
- The API is expected to return valid institution data in production
- The manual add feature is not currently implemented and won't be in the near future

## Acceptance Criteria

### AC-1: Mock Data Removed
- **Given**: The page loads and API call fails
- **When**: Looking at the error handling code
- **Then**: There should be no mock data fallback, only error logging
- **Verification**: `programmatic`

### AC-2: Manual Add Card Removed
- **Given**: The page is rendered
- **When**: Viewing the institutions list
- **Then**: The "手动添加其他机构" card should not be visible
- **Verification**: `human-judgment`

### AC-3: Code Cleanliness
- **Given**: The file is reviewed
- **When**: Checking for unused code
- **Then**: No dead code or unused functions should remain
- **Verification**: `human-judgment`

## Open Questions
- [ ] None