---
name: context-audit
description: Audit what this Claude Code setup loads into context every session and recommend trims. Run manually with /context-audit.
disable-model-invocation: true
context: fork
agent: general-purpose
argument-hint: "[optional focus: mcp | plugins | claude-md | agents]"
---

# Context audit

Audit the Claude Code configuration for this repo and this user. Find tokens that load on
every request (or every subagent spawn) and don't earn their place, then recommend specific,
low-risk trims. **Do not change any config file.** Report only; the user approves changes.

Focus area from the user (may be empty): $ARGUMENTS

## What actually costs context (judge findings against this)

| Thing | When it costs | How much |
|---|---|---|
| CLAUDE.md (all levels) + `@imports` | Every request, and every subagent except Explore/Plan or ones with `omitClaudeMd: true` | Full text |
| `.claude/rules/*.md` without `paths:` | Every request | Full text |
| `.claude/rules/*.md` with `paths:` | Only when matching files are touched | Full text |
| Output style (non-default) | Every request | Full text |
| Skills (model-invocable) | Every request | Name + description |
| Skills with `disable-model-invocation: true` | Only when the user runs them | Zero until invoked |
| Subagent definitions | Every request | Name + description |
| Plugins | Every request | Names + descriptions of their skills, agents, commands |
| MCP servers, tool search on | Every request | Tool names + server instructions (schemas deferred) |
| MCP servers, tool search off | Every request | Full definition of every tool |
| Hooks | Only if the hook prints output that Claude reads | Zero otherwise |

## Step 1 — Gather (cheap commands; keep counts, never paste whole files back)

If a command fails, note it and move on.

1. **Instruction files** — path, line count, estimated tokens (chars ÷ 4):
   `~/.claude/CLAUDE.md`, `./CLAUDE.md`, `./CLAUDE.local.md`, nested `**/CLAUDE.md`
   (skip `node_modules`, `target`, `build`, `dist`), `./.claude/rules/**/*.md`,
   `~/.claude/rules/**/*.md` (note which have `paths:`), and anything pulled in by `@path` imports.
2. **Skills** — `~/.claude/skills/*/SKILL.md`, `./.claude/skills/*/SKILL.md`: name,
   description length, `disable-model-invocation` set?, side effects? (deploys, pushes,
   Jira writes, messages).
3. **Subagents** — `~/.claude/agents/**/*.md`, `./.claude/agents/**/*.md`: name,
   description length, `model`, `tools` restricted?, `omitClaudeMd`?
4. **MCP** — `claude mcp list`: name, scope, and whether a CLI equivalent exists here
   (`which gh az kubectl jq aws gcloud`).
5. **Plugins** — `claude plugin list`, then `claude plugin details <name>` for each enabled
   plugin. Record the **Always-on** token figure.
6. **Settings** — `~/.claude/settings.json`, `./.claude/settings.json`,
   `./.claude/settings.local.json`: `hooks` (and whether each prints output), `outputStyle`,
   `model`, `effortLevel`, `env` (e.g. `CLAUDE_CODE_SUBAGENT_MODEL`), `permissions.deny` `Read(...)` rules.
   Also whether `ANTHROPIC_BASE_URL` or `ENABLE_TOOL_SEARCH` is set (shell or settings `env`):
   a non-Anthropic `ANTHROPIC_BASE_URL` turns MCP tool search off by default.
7. **Generated dirs** — do `target/`, `build/`, `dist/`, `node_modules/`, `coverage/`,
   `.gradle/` exist, and are reads of them denied?
8. **Previous audits** — if `.claude/audits/` exists, read the latest for comparison.

## Step 2 — Judge (priority order)

1. **CLAUDE.md over ~200 lines total**, or workflow-specific sections (PR process, release
   steps, long examples) → move to a skill.
2. **Rules without `paths:`** that are clearly language/folder-specific → add `paths:`.
3. **`@imports` of large docs** → make a skill, or reference the path so Claude reads it on demand.
4. **Plugins with always-on ≥ 2,000 tokens**, or that look unused → disable per-project or
   uninstall (confirm with `/doctor` and `/skill-doctor`).
5. **MCP servers with a CLI equivalent** (GitHub → `gh`, Azure → `az`) → prefer the CLI.
   **MCP servers only one workflow needs** → remove from `.mcp.json`, declare inline in that
   subagent's `mcpServers:` so the main session never carries the tools.
   **Tool search off** (gateway `ANTHROPIC_BASE_URL`, or `ENABLE_TOOL_SEARCH=false`) → every
   MCP schema loads up front; check whether the gateway forwards `tool_reference` blocks so
   `ENABLE_TOOL_SEARCH=true` can be set, and trim servers harder until then.
6. **Skills with side effects, or rarely used**, still model-invocable → add
   `disable-model-invocation: true`.
7. **Skill/agent descriptions over ~250 chars** → shorten; move detail into the body.
8. **Hooks that print to context** (SessionStart / UserPromptSubmit context, PostToolUse
   linters dumping full output) → filter to errors, cap lines.
9. **Subagents with no `model:`** doing simple work (search, summarise) → `sonnet` or
   `haiku`. Subagents that don't need project conventions → `omitClaudeMd: true`.
10. **No `Read` deny for generated dirs** → suggest `permissions.deny` entries like
    `Read(./target/**)`, `Read(./node_modules/**)`.

Don't flag things that are cheap and working. Never recommend removing safety hooks or
permission rules.

## Step 3 — Report

Write the full report to `.claude/audits/context-<YYYY-MM-DD>.md` (create the folder if
needed): a table of every item gathered (path, type, est. tokens, verdict), the ranked fix
list with the exact edit for each as a snippet or diff, and anything you couldn't check.

Return **only** this summary to the main conversation (≤ 25 lines):

```
Always-on estimate: ~N tokens (last audit: ~M, Δ ±X)

Top fixes (est. saving):
1. … (~N tok) — what to change, where
2. …
3. …

Watch list: …
Full report: .claude/audits/context-YYYY-MM-DD.md
Confirm real numbers with: /context all
```
