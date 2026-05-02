# Implement from Spec

Run Codex against the approved spec at `/tmp/spec.md`.

## Instructions

Run the following and show the output:

```bash
test -f /tmp/spec.md || { echo "ERROR: /tmp/spec.md not found. Run /spec first."; exit 1; }
{ cat /tmp/spec.md; echo -e "\nAfter implementation, write a summary of all changes to /tmp/reply.txt"; } | codex exec --full-auto
cat /tmp/reply.txt
```
