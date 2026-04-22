# Security policy

## Scope

This project is a university coursework submission and is **not intended
for production use**. It runs an in-memory data store with no
authentication or authorisation. Do not expose it to the public
internet.

## Threat model caveats

- No auth — every endpoint is anonymous and mutable.
- No TLS — the embedded Grizzly server speaks plain HTTP.
- No rate limiting — a single client can trivially flood the store.
- No persistence — restarting the process wipes all data.

Any real deployment would need to address all four. See
[deployment.md](docs/deployment.md) for hardening notes.

## Reporting a vulnerability

If you discover a vulnerability in the code as submitted (e.g. an
exception that leaks internal state, or an injection vector in the
JSON handling) please open a **private** security advisory on the
GitHub repo rather than a public issue.

Include:

- A minimal reproducer (curl is fine).
- Your read of the impact — what a hostile caller could achieve.
- Version / commit hash you observed the issue on.

You'll get an acknowledgement within a few days. Since this is a
single-developer student project there is no formal SLA for fixes, but
the goal is to respond to realistic reports within one working week.

## Supported versions

Only the current `main` branch is supported. No patches are published
against older tags.
