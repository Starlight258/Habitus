#!/usr/bin/env python3
"""Post a PR comment as a GitHub App persona.

Usage:
    post_as_persona.py <persona> <owner/repo> <pr_number> <body_file>
    post_as_persona.py <persona> <owner/repo> <pr_number> <body_file> --reply-to <comment_id>

If --reply-to is provided, posts as a threaded reply to that inline comment.
Otherwise, posts as an inline PR comment on the first `file:line` found in the body.
Falls back to a plain PR review comment if no file:line is found or line is not in diff.

Saves the comment ID to /tmp/comment_id_<persona>.txt for later use in replies.
"""

import sys
import json
import re
import time
import os

try:
    import jwt
    import requests
except ImportError:
    print("Missing dependencies. Run: .claude/venv/bin/pip install PyJWT cryptography requests")
    sys.exit(1)

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "personas.json")


def get_installation_token(app_id, pem_path, installation_id):
    with open(pem_path) as f:
        private_key = f.read()
    now = int(time.time())
    payload = {"iat": now - 60, "exp": now + 600, "iss": app_id}
    jwt_token = jwt.encode(payload, private_key, algorithm="RS256")
    resp = requests.post(
        f"https://api.github.com/app/installations/{installation_id}/access_tokens",
        headers={
            "Authorization": f"Bearer {jwt_token}",
            "Accept": "application/vnd.github.v3+json",
        },
    )
    resp.raise_for_status()
    return resp.json()["token"]


def get_pr_info(token, owner, repo, pr_number):
    """Return (list of filenames, latest commit sha)."""
    resp = requests.get(
        f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3+json",
        },
    )
    resp.raise_for_status()
    pr = resp.json()
    commit_sha = pr["head"]["sha"]

    files_resp = requests.get(
        f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}/files",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3+json",
        },
    )
    files_resp.raise_for_status()
    filenames = [f["filename"] for f in files_resp.json()]
    return filenames, commit_sha


def parse_file_line(body):
    """Extract first `file:line` reference from review body."""
    match = re.search(r'`([^`\s:]+\.(?:java|kt|py|ts|js|go|rs)):(\d+)`', body)
    if match:
        return match.group(1), int(match.group(2))
    return None, None


def find_full_path(filename, pr_files):
    if filename in pr_files:
        return filename
    for path in pr_files:
        if path.endswith("/" + filename) or path == filename:
            return path
    return None


def post_inline_comment(token, owner, repo, pr_number, commit_sha, path, line, body):
    """Post inline PR review comment. Returns (comment_id, html_url)."""
    resp = requests.post(
        f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}/comments",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3+json",
        },
        json={"body": body, "commit_id": commit_sha, "path": path, "line": line, "side": "RIGHT"},
    )
    resp.raise_for_status()
    data = resp.json()
    return data["id"], data["html_url"]


def post_reply(token, owner, repo, pr_number, comment_id, body):
    """Post threaded reply to an inline comment. Returns (comment_id, html_url)."""
    resp = requests.post(
        f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}/comments/{comment_id}/replies",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3+json",
        },
        json={"body": body},
    )
    resp.raise_for_status()
    data = resp.json()
    return data["id"], data["html_url"]


def post_review(token, owner, repo, pr_number, body):
    """Fallback: plain PR review comment. Returns (review_id, html_url)."""
    resp = requests.post(
        f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}/reviews",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3+json",
        },
        json={"body": body, "event": "COMMENT"},
    )
    resp.raise_for_status()
    data = resp.json()
    return data["id"], data["html_url"]


def main():
    args = sys.argv[1:]

    reply_to = None
    if "--reply-to" in args:
        idx = args.index("--reply-to")
        reply_to = int(args[idx + 1])
        args = args[:idx] + args[idx + 2:]

    if len(args) < 4:
        print("Usage: post_as_persona.py <persona> <owner/repo> <pr_number> <body_file> [--reply-to <id>]")
        sys.exit(1)

    persona, repo, pr_number, body_file = args[0], args[1], int(args[2]), args[3]

    with open(CONFIG_PATH) as f:
        config = json.load(f)

    if persona not in config:
        print(f"Unknown persona '{persona}'. Available: {list(config.keys())}")
        sys.exit(1)

    c = config[persona]
    with open(body_file) as f:
        body = f.read()

    token = get_installation_token(c["app_id"], c["pem_path"], c["installation_id"])
    owner, repo_name = repo.split("/")

    if reply_to:
        comment_id, url = post_reply(token, owner, repo_name, pr_number, reply_to, body)
    else:
        filename, line = parse_file_line(body)
        comment_id, url = None, None

        if filename and line:
            pr_files, commit_sha = get_pr_info(token, owner, repo_name, pr_number)
            full_path = find_full_path(filename, pr_files)
            if full_path:
                try:
                    comment_id, url = post_inline_comment(token, owner, repo_name, pr_number, commit_sha, full_path, line, body)
                except Exception as e:
                    print(f"Warning: inline comment failed ({e}), falling back to review", file=sys.stderr)

        if not comment_id:
            comment_id, url = post_review(token, owner, repo_name, pr_number, body)

    print(url)

    with open(f"/tmp/comment_id_{persona}.txt", "w") as f:
        f.write(str(comment_id))

    # Save whether this was inline or review (reply API only works for inline)
    comment_type = "inline" if (not reply_to and filename and line and comment_id and "review" not in url.split("#")[-1]) else "review"
    with open(f"/tmp/comment_type_{persona}.txt", "w") as f:
        f.write(comment_type)


if __name__ == "__main__":
    main()
