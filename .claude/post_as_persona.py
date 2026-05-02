#!/usr/bin/env python3
"""Post a PR comment as a GitHub App persona.

Usage:
    post_as_persona.py <persona> <owner/repo> <pr_number> <body_file>

Example:
    post_as_persona.py linus Starlight258/Habitus 42 /tmp/review_linus.md
"""

import sys
import json
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


def post_review(token, owner, repo, pr_number, body):
    """Post as a PR review (uses pull_requests: write, no issues permission needed)."""
    resp = requests.post(
        f"https://api.github.com/repos/{owner}/{repo}/pulls/{pr_number}/reviews",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3+json",
        },
        json={"body": body, "event": "COMMENT"},
    )
    resp.raise_for_status()
    return resp.json()["html_url"]


def main():
    if len(sys.argv) < 5:
        print("Usage: post_as_persona.py <persona> <owner/repo> <pr_number> <body_file>")
        sys.exit(1)

    persona = sys.argv[1]
    repo = sys.argv[2]
    pr_number = int(sys.argv[3])
    body_file = sys.argv[4]

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
    url = post_review(token, owner, repo_name, pr_number, body)
    print(url)


if __name__ == "__main__":
    main()
