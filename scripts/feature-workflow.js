#!/usr/bin/env node

/**
 * Feature Workflow: Design → Implement → PR
 * Usage: node scripts/feature-workflow.js "Feature Description" "branch-name"
 *
 * Example:
 *   node scripts/feature-workflow.js \
 *     "Add activity template management API" \
 *     "feat/add-activity-template-management-api"
 */

const { execSync, spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const colors = {
  reset: '\x1b[0m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
};

const log = {
  info: (msg) => console.log(`${colors.blue}${msg}${colors.reset}`),
  success: (msg) => console.log(`${colors.green}✓ ${msg}${colors.reset}`),
  warn: (msg) => console.log(`${colors.yellow}⚠ ${msg}${colors.reset}`),
  error: (msg) => console.error(`${colors.red}✗ ${msg}${colors.reset}`),
};

const [featureDesc, branchName] = process.argv.slice(2);

if (!featureDesc || !branchName) {
  log.error('Usage: node scripts/feature-workflow.js "Feature Description" "branch-name"');
  console.log('\nExample:');
  console.log('  node scripts/feature-workflow.js \\');
  console.log('    "Add activity template management API" \\');
  console.log('    "feat/add-activity-template-management-api"');
  process.exit(1);
}

const repoRoot = path.resolve(__dirname, '..');
process.chdir(repoRoot);

async function runCommand(cmd, options = {}) {
  const { silent = false, input = null } = options;
  try {
    const result = execSync(cmd, {
      encoding: 'utf-8',
      stdio: silent ? 'pipe' : 'inherit',
      cwd: repoRoot,
    });
    return result.trim();
  } catch (error) {
    if (!silent) throw error;
    return error.stdout?.trim() || '';
  }
}

async function main() {
  console.log(`\n${colors.blue}=== Feature Workflow: Design → Implement → PR ===${colors.reset}\n`);

  try {
    // Step 1: Create/switch branch
    log.info('[1/5] Creating feature branch: ' + branchName);
    try {
      runCommand(`git show-ref --quiet refs/heads/${branchName}`, { silent: true });
      log.warn('Branch already exists, checking out...');
      runCommand(`git checkout ${branchName}`);
    } catch {
      runCommand(`git checkout -b ${branchName}`);
      log.success('Branch created');
    }
    console.log('');

    // Step 2: Run claude /design
    log.info('[2/5] Running design phase...');
    try {
      runCommand(`claude /design "${featureDesc}"`);
      log.success('Design specification created at /tmp/spec.md');
    } catch (error) {
      log.warn('Design phase output (check /tmp/spec.md)');
    }

    if (!fs.existsSync('/tmp/spec.md')) {
      log.error('/tmp/spec.md not found. Design phase may have failed.');
      process.exit(1);
    }
    console.log('');

    // Step 3: Run claude /implement
    log.info('[3/5] Running implementation phase...');
    const replyFile = `/tmp/reply_${Date.now()}.txt`;
    const specContent = fs.readFileSync('/tmp/spec.md', 'utf-8');
    const implInput = specContent + '\n\nAfter implementation, write a summary of all changes to ' + replyFile;

    try {
      runCommand(`echo '${implInput.replace(/'/g, "'\\''")}' | claude exec --full-auto`);
      log.success('Implementation completed');
    } catch (error) {
      log.warn('Implementation phase output');
    }
    console.log('');

    // Step 4: Show implementation summary
    if (fs.existsSync(replyFile)) {
      log.info('[4/5] Implementation Summary:');
      const summary = fs.readFileSync(replyFile, 'utf-8');
      console.log(summary);
      console.log('');
    }

    // Step 5: Commit and PR
    log.info('[5/5] Creating commit and preparing PR...');

    // Check for changes
    const statusOutput = runCommand('git status --porcelain', { silent: true });
    if (!statusOutput) {
      log.warn('No changes detected. Codex may not have made modifications.');
      process.exit(0);
    }

    // Stage changes
    runCommand('git add -A');
    log.success('Changes staged');

    // Create commit
    const commitMsg = `feat: ${featureDesc}

- Implementation of activity template management API
- Includes CRUD endpoints for managing activity templates
- Integrated with existing recommendation service

Co-Authored-By: Claude Haiku 4.5 <noreply@anthropic.com>`;

    try {
      runCommand(`git commit -m "${commitMsg.replace(/"/g, '\\"')}"`);
      log.success('Commit created');
    } catch {
      log.warn('No new changes to commit');
      process.exit(0);
    }
    console.log('');

    // Push branch
    log.info('Pushing branch to remote...');
    runCommand(`git push -u origin ${branchName}`, { silent: true });
    log.success('Branch pushed');
    console.log('');

    // Create PR
    log.info('Creating pull request...');
    try {
      const prOutput = runCommand(`gh pr create --title "feat: ${featureDesc}" --body "## Summary

This PR implements: **${featureDesc}**

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
- [ ] Documentation updated if needed" --base main --head ${branchName}`, { silent: true });

      const prUrl = prOutput.match(/https:\/\/github\.com[^\s]+/) || [];
      if (prUrl[0]) {
        log.success('Pull request created');
        console.log(`${colors.green}PR URL: ${prUrl[0]}${colors.reset}`);
      } else {
        log.warn('Could not retrieve PR URL. PR may have been created.');
      }
    } catch (error) {
      log.warn('gh CLI not found. Create PR manually at GitHub');
    }

    console.log('');
    console.log(`${colors.green}=== Workflow Complete ===${colors.reset}\n`);
    console.log('Next steps:');
    console.log('1. Review the PR at GitHub');
    console.log('2. Make any necessary adjustments');
    console.log('3. Merge when ready\n');

  } catch (error) {
    log.error('Workflow failed: ' + error.message);
    process.exit(1);
  }
}

main();
