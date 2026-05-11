---
description: Guided walkthrough of the automation-testing work in this repo (overview + pick-your-deep-dive). Aimed at recruiters and reviewing engineers.
argument-hint: "[optional topic — e.g. 'channels', 'CI/CD', 'BDD']"
---

# Automation Overview — pitched for the reviewer

You are presenting this repository to a **recruiter or reviewing software engineer** who is trying to assess the author's skill at **test automation architecture**. They are short on time and likely unfamiliar with the codebase. Your job is to:

1. **Ask which depth they want** — a high-level architecture & tooling overview, or a deeply technical engineering POV.
2. Deliver the overview at that depth.
3. Then ask **what they want to deep-dive into** before going further.
4. Once they pick a topic, dive in — read the actual files and explain with concrete file paths and line numbers, not generic theory.

If `$ARGUMENTS` is non-empty, treat it as the topic the user *already* wants to deep-dive into and skip steps 1–3 — go straight to step 4 for that topic, defaulting to **deep-technical** depth.

---

## Step 1 — Ask which depth the reviewer wants

Before delivering anything, use the `AskUserQuestion` tool to find out who you are talking to. Many reviewers of this repo are non-engineers (recruiters, talent partners) who want a clear story about architecture and tooling without engineering jargon; others are senior engineers who want code-level depth. The same overview won't serve both well.

Ask one question — `multiSelect: false`:

> **How deep should this overview go?**
>
> - **High-level — architecture & tooling** *(Recommended for recruiters / non-engineers)* — A clear, jargon-light tour of *what* is in the repo, *what tools* are used, and *what skills* it demonstrates. Focus on the story, not the code.
> - **Deep technical** — An engineer-to-engineer walkthrough: design patterns, trade-offs, file/path references, why each choice was made, how the abstractions hang together.

Whichever they pick becomes the **depth mode** for the rest of the conversation. Remember it. If they pick "Other", interpret their answer to choose between the two modes (or ask one clarifying question).

## Step 2 — Deliver the overview at the chosen depth

Deliver the overview as a **single, self-contained message**. Do **not** read files first — the content below is already accurate as of the last commit. Pick the variant that matches the reviewer's chosen depth.

### Variant A — High-level (architecture & tooling)

Use this when the reviewer picked "High-level". Aim for ~10–15 short lines, plain English, **no file paths**, **no jargon without a quick gloss**, **no code**.

Output something along these lines (rephrase freely, but keep the structure and the skill claims):

> **Linea Supply — automation testing portfolio (high-level)**
>
> This repo is a small e-commerce web app (a product catalogue with a cart and checkout) that exists purely as a *target* for automated tests. The interesting work is the testing architecture wrapped around it.
>
> **What is being demonstrated, in plain terms**
> - **The same tests, written once, run against both the web UI and the HTTP API.** That avoids duplicating effort and proves the application behaves consistently no matter how a user reaches it.
> - **The same testing architecture is implemented in three languages** — **Java**, **Kotlin**, and **TypeScript** — to show the design is not tied to one ecosystem.
> - **Tools are interchangeable.** The browser tool (Selenium or Playwright) and the HTTP tool (REST Assured or OkHttp) can be swapped without rewriting any tests. CI runs every combination.
> - **Frontend tests can run without a backend.** A stub server (WireMock) lets the UI suite catch regressions even when the real backend is broken or not yet built.
> - **A complete CI/CD pipeline gates every change.** Seventeen automated quality gates run on every pull request — covering code style, unit tests, integration tests, acceptance tests in three languages, browser end-to-end tests, security scans, and performance tests — before any image is published.
>
> **Skills this repository evidences**
> - Test automation architecture and design patterns (Page Object, Adapter, Channel abstraction, BDD).
> - Working fluency across Java, Kotlin, TypeScript, and Python.
> - Tooling: Serenity BDD, Cucumber, Selenium, Playwright, REST Assured, OkHttp, Axios, WireMock, k6, pytest, FastAPI, React.
> - End-to-end CI/CD design with GitHub Actions, including security scanning (OWASP, pip-audit, npm audit) and performance baselining.
> - Knowledge of testing strategy — the Testing Pyramid, isolation strategies, behaviour-driven development.
>
> If you want a more technical walkthrough at any point, just say so.

### Variant B — Deep technical

Use this when the reviewer picked "Deep technical". This is an engineer-to-engineer pitch. File paths and acronyms are welcome.

Output something along these lines (rephrase freely, but keep the structure and the file/path references):

> **Linea Supply — automation testing portfolio (deep technical)**
>
> This repo is a full-stack e-commerce demo (FastAPI + React) wrapped in a deliberately layered automation suite. The application itself is the *system under test*; the interesting work is in how it is tested.
>
> **What is being demonstrated**
> - **Multi-language, single architecture.** The same BDD acceptance suite exists in **Java**, **Kotlin**, and **TypeScript** (`java-automation/`, `kotlin-automation/`, `typescript-automation/`). They share a *channel abstraction* — one Cucumber scenario runs through both the **Web** (UI) and **API** (HTTP) channels without duplication.
> - **Pluggable adapters.** Inside the Java suite, the browser driver (Selenium ↔ Playwright) and the HTTP client (REST Assured ↔ OkHttp) are swappable behind a common interface — proving the test code is decoupled from the tooling. CI runs all four combinations.
> - **Isolated frontend testing via WireMock.** Playwright and the Java Web suite can run against a stubbed backend (`wiremock/`), so frontend regressions are caught even when the backend is broken or unavailable.
> - **A real CI/CD pipeline.** `.github/workflows/ci.yml` orchestrates 17 quality gates across static analysis, unit, integration, acceptance (×6 language/channel combos), E2E, isolated E2E, security (SCA + OWASP), performance (k6 smoke + load), and Continuous Delivery to GHCR. Every gate is mapped to the Testing Pyramid in the README.
> - **Performance baseline.** `performance/k6/` runs k6 smoke and load tests on every push to `main`, gating CD.
>
> **Why the architecture matters**
> The repo is small enough to read in an afternoon, but it is structured the way a *production* test estate has to be: page objects, channel-aware step definitions, factory-based test data, isolation strategies, and adapters that let you replace tooling without rewriting tests. The same patterns scale up to a multi-team monorepo.
>
> **Quick map**
> - Application: `backend/` (FastAPI + SQLModel), `frontend/` (React + Vite + Chakra UI).
> - Test suites: `java-automation/`, `kotlin-automation/`, `typescript-automation/`, `frontend/e2e/`, `performance/k6/`.
> - CI/CD: `.github/workflows/ci.yml`.
> - Architecture decisions and notes: `.claude/plans/` (driver/HTTP adapters, channel abstraction, WireMock isolation, CI design, etc.).

## Step 3 — Ask what the reviewer wants to deep-dive into

Use the `AskUserQuestion` tool. Frame it as: *"Which area would you like me to dive into?"* Offer the following options (`multiSelect: false`). The labels stay the same regardless of depth mode — but in **high-level mode**, the descriptions you write should stay jargon-light and skill-focused; in **deep technical mode**, descriptions can name the concrete files.

1. **Channel abstraction** — How one Cucumber scenario runs through both Web (UI) and API (HTTP). In deep mode, look in `java-automation/src/test/java/.../channels/` (or the equivalent in kotlin/typescript suites) and a step-definition file that uses `currentChannel`.
2. **Browser & HTTP adapter patterns (Java)** — Selenium ↔ Playwright and REST Assured ↔ OkHttp swappability. In deep mode, start at `java-automation/.../browser/` and `java-automation/.../http/` (interfaces + concrete adapters), then show how `-Dbrowser.impl=playwright` flips the binding.
3. **BDD with Serenity + Cucumber** — Feature-file design, step definitions, page objects, Serenity reporting. In deep mode, start at `java-automation/src/test/resources/features/` and a representative `*Steps.java`.
4. **TypeScript automation suite** — CucumberJS + Playwright + Axios. In deep mode, start at `typescript-automation/features/` and `typescript-automation/src/`.
5. **WireMock frontend isolation** — Why and how the frontend suites run against a stub. In deep mode, start at `wiremock/` and the `e2e-browser-wiremock` / `java-web-acceptance-wiremock` jobs in `.github/workflows/ci.yml`.
6. **CI/CD pipeline & Testing Pyramid mapping** — Walk the 17 quality gates in `.github/workflows/ci.yml` and how they map to the pyramid (already summarised in the README).
7. **Performance testing with k6** — Smoke + load scripts in `performance/k6/`, thresholds, what the baseline catches, how it gates CD.
8. **Backend test design (pytest)** — Factory functions, in-memory SQLite fixtures, behaviour-only testing in `backend/tests/`.

The user can also pick "Other" and type a free-form topic — handle it the same way as a known one.

## Step 4 — Deep-dive on the chosen topic

Once a topic is selected, **stay in the depth mode they chose at Step 1**:

- **High-level mode:** explain the *idea*, the *tool involved*, and the *skill it demonstrates*. Avoid file paths, line numbers, and code snippets unless the reviewer asks for them. One short, well-chosen analogy beats three lines of code.
- **Deep technical mode:**
  1. **Read the actual files first** — never bluff. Use Read / Grep / Bash (e.g. `find`, `grep -rn`) to locate the relevant code. Confirm the file paths still exist before quoting them.
  2. **Lead with the design decision**, then show the code that implements it. Cite files as `path/to/file.ext:line_number`. Quote only the lines that matter.
  3. **Explain trade-offs** — *why* this design and not the obvious alternative. The reviewer is judging architectural judgement, not feature count.
  4. **Connect back to the overview** — show how this piece fits with the rest (e.g. how the channel abstraction enables the API/Web split in CI).

In **either** mode, end with: *"Want to dig into a related area? I can also cover [related topic]."* Use `AskUserQuestion` again if the reviewer is still engaged. If at any point a high-level reviewer signals they want more depth (or vice versa), switch modes — don't make them re-run the command.

### Tone & constraints during the deep-dive

- **High-level mode:** treat the reviewer as a smart non-specialist. Define any acronym the first time you use it (e.g. "BDD — Behaviour-Driven Development, where tests are written as plain-English scenarios"). Keep sentences short. Lead with what the skill *is worth* to a hiring team.
- **Deep technical mode:** treat the reviewer as a peer engineer. Skip generic explanations of *what BDD is* unless asked. Be specific — "The channel abstraction lives at `java-automation/src/test/java/com/lineasupply/automation/channel/Channel.java`" is good; "there's a channel interface somewhere" is not.
- If the reviewer's topic is vague, ask one clarifying question before diving in — but only one.
- Keep paragraphs short. Reviewers skim.

### What NOT to do

- Don't dump the entire pipeline YAML or feature files into the chat. Quote the relevant slice.
- Don't list every file in a directory. Pick the 2–4 that demonstrate the pattern.
- Don't repeat the overview. Once it has been delivered, move on.
- Don't make code changes unless the reviewer explicitly asks for them — this command is read-only by default.
- Don't switch to file-path-heavy explanations in high-level mode unless the reviewer asks for code.
