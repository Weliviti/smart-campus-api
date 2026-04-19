# Contributing

This project is a university coursework submission for **5COSC022W Client-Server
Architectures**. External pull requests aren't expected, but if you're the author
coming back to it, the conventions below will keep the history consistent.

## Commit style

Conventional Commits:

```
<type>(<scope>): <short summary>
```

Allowed types:

- `feat`     — new behaviour or endpoint
- `fix`      — bug fix
- `test`     — test-only changes
- `docs`     — documentation only
- `chore`    — tooling, config, build plumbing
- `refactor` — code change that is not a feat or fix
- `build`    — changes to `pom.xml`, shade plugin, or dependencies
- `ci`       — GitHub Actions / workflow changes

Scope is the module the change touches: `app`, `resource`, `model`, `store`,
`mapper`, `filter`, `exception`, `test`, `docs`.

Examples:

- `feat(resource): support ?type= filter on /sensors`
- `test(store): cover addSensorIdToRoom race condition`
- `docs: document 422 error scenario`

## Branches

- `main` is the protected integration branch.
- Feature work on short-lived branches named `feat/<something>` or
  `fix/<something>`.
- Rebase before opening a PR; squash merge into `main`.

## Running tests locally

```powershell
mvn -q test
```

A full verify (compile + tests + shade):

```powershell
mvn -q verify
```

## Style

- Java 11 syntax, 4-space indent (see `.editorconfig`).
- Public classes and non-trivial methods get a short Javadoc.
- No dependency on `jakarta.*` — stick with `javax.*` since Jersey 2.41 targets it.
- Keep resource classes thin: validation + delegation. Business rules live in
  the service/store.
