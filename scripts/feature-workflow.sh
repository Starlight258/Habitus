#!/bin/bash

# Feature Workflow: Design → Implement → Create PR
# Usage: ./scripts/feature-workflow.sh "Feature Description" "branch-name"

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Parse arguments
FEATURE_DESC="${1:-}"
BRANCH_NAME="${2:-}"

if [ -z "$FEATURE_DESC" ] || [ -z "$BRANCH_NAME" ]; then
    echo -e "${RED}Usage: $0 \"Feature Description\" \"branch-name\"${NC}"
    echo ""
    echo "Example:"
    echo "  $0 \"Add activity template management API\" \"feat/add-activity-template-management-api\""
    exit 1
fi

REPO_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$REPO_ROOT"

echo -e "${BLUE}=== Feature Workflow: Design → Implement → PR ===${NC}"
echo ""

# Step 1: Create/Switch to feature branch
echo -e "${BLUE}[1/5] Creating feature branch: $BRANCH_NAME${NC}"
if git show-ref --quiet refs/heads/"$BRANCH_NAME"; then
    echo -e "${YELLOW}Branch already exists, checking out...${NC}"
    git checkout "$BRANCH_NAME"
else
    git checkout -b "$BRANCH_NAME"
    echo -e "${GREEN}✓ Branch created${NC}"
fi
echo ""

# Step 2: Run /design skill (via claude code)
echo -e "${BLUE}[2/5] Running design phase...${NC}"
if ! command -v claude &> /dev/null; then
    echo -e "${RED}Error: 'claude' command not found. Please install Claude Code CLI.${NC}"
    exit 1
fi

# Create a temporary file for the design command
DESIGN_INPUT="/tmp/design_input_$$.txt"
echo "$FEATURE_DESC" > "$DESIGN_INPUT"

# Run design and capture if successful
if claude /design "$FEATURE_DESC" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Design specification created at /tmp/spec.md${NC}"
else
    echo -e "${YELLOW}⚠ Design completed with status (check /tmp/spec.md)${NC}"
fi
echo ""

# Verify spec.md exists
if [ ! -f /tmp/spec.md ]; then
    echo -e "${RED}Error: /tmp/spec.md not found. Design phase may have failed.${NC}"
    exit 1
fi
echo ""

# Step 3: Run /implement skill
echo -e "${BLUE}[3/5] Running implementation phase...${NC}"
REPLY_FILE="/tmp/reply_$$.txt"

# Run implementation
if { cat /tmp/spec.md; echo -e "\nAfter implementation, write a summary of all changes to $REPLY_FILE"; } | claude exec --full-auto 2>&1 | tee /tmp/impl_log.txt; then
    echo -e "${GREEN}✓ Implementation completed${NC}"
else
    echo -e "${YELLOW}⚠ Implementation completed (check logs)${NC}"
fi
echo ""

# Step 4: Check for implementation results
if [ -f "$REPLY_FILE" ]; then
    echo -e "${BLUE}[4/5] Implementation Summary:${NC}"
    cat "$REPLY_FILE"
    echo ""
else
    echo -e "${YELLOW}⚠ No implementation summary found${NC}"
fi
echo ""

# Step 5: Create commit and PR
echo -e "${BLUE}[5/5] Creating commit and preparing PR...${NC}"

# Check for uncommitted changes
if git diff --quiet && git diff --cached --quiet; then
    echo -e "${YELLOW}No changes detected. Codex may not have made modifications.${NC}"
    exit 0
fi

# Stage all changes
git add -A
echo -e "${GREEN}✓ Changes staged${NC}"

# Create commit with feature description
COMMIT_MSG="feat: $FEATURE_DESC

- Implementation of activity template management API
- Includes CRUD endpoints for managing activity templates
- Integrated with existing recommendation service

Co-Authored-By: Claude Haiku 4.5 <noreply@anthropic.com>"

git commit -m "$COMMIT_MSG" || {
    echo -e "${YELLOW}No new changes to commit${NC}"
    exit 0
}

echo -e "${GREEN}✓ Commit created${NC}"
echo ""

# Push branch
echo -e "${BLUE}Pushing branch to remote...${NC}"
git push -u origin "$BRANCH_NAME" 2>&1 | grep -v "^remote:" || true
echo -e "${GREEN}✓ Branch pushed${NC}"
echo ""

# Create PR using gh CLI
echo -e "${BLUE}Creating pull request...${NC}"
if ! command -v gh &> /dev/null; then
    echo -e "${YELLOW}⚠ 'gh' CLI not found. Please create PR manually at:${NC}"
    echo "  https://github.com/audwl03071/Habitus/compare/main...$BRANCH_NAME"
    exit 0
fi

PR_URL=$(gh pr create \
    --title "feat: $FEATURE_DESC" \
    --body "## Summary

This PR implements: **$FEATURE_DESC**

## Changes
- See commit for detailed changes
- Run tests to verify implementation

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project conventions
- [ ] No breaking changes
- [ ] Documentation updated if needed" \
    --base main \
    --head "$BRANCH_NAME" 2>&1 | grep "https://" | head -1)

if [ -n "$PR_URL" ]; then
    echo -e "${GREEN}✓ Pull request created${NC}"
    echo -e "${GREEN}PR URL: $PR_URL${NC}"
else
    echo -e "${YELLOW}⚠ Could not retrieve PR URL. PR may have been created.${NC}"
fi

echo ""
echo -e "${GREEN}=== Workflow Complete ===${NC}"
echo ""
echo "Next steps:"
echo "1. Review the PR at GitHub"
echo "2. Make any necessary adjustments"
echo "3. Merge when ready"
