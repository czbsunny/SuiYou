# Search Institution Page Cleanup - Implementation Plan

## [x] Task 1: Remove mock data function and usage
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - Remove the `getMockInstitutions` function
  - Remove the fallback call to `getMockInstitutions` in the catch block of `loadInstitutions`
- **Acceptance Criteria Addressed**: [AC-1, AC-3]
- **Test Requirements**:
  - `programmatic` TR-1.1: File compiles without syntax errors
  - `human-judgement` TR-1.2: No mock data function exists in the file
- **Notes**: Update the catch block to only log the error

## [x] Task 2: Remove manual add institution functionality
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - Remove the `handleManualAdd` function
  - Remove the manual add card from the template
  - Remove the related styles
- **Acceptance Criteria Addressed**: [AC-2, AC-3]
- **Test Requirements**:
  - `programmatic` TR-2.1: File compiles without syntax errors
  - `human-judgement` TR-2.2: No manual add card in the rendered page
- **Notes**: Remove all related CSS classes for the manual add card

## [x] Task 3: Verify page functionality
- **Priority**: P1
- **Depends On**: Task 1, Task 2
- **Description**: 
  - Verify the page still loads correctly
  - Verify search functionality works
  - Verify tab switching works
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `programmatic` TR-3.1: No console errors on page load
  - `human-judgement` TR-3.2: Page UI looks correct without manual add card
- **Notes**: Test in development environment