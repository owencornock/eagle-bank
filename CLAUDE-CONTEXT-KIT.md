# Claude Code context kit

Two things: a `/context-audit` you run periodically to find what's eating context, and a
lead + sub-agent setup for tickets where each agent only gets what it needs.

Facts below checked against the Claude Code docs on 8 Oct 2026.

## What actually uses context

| | Cost every request? | Notes |
|---|---|---|
| CLAUDE.md, rules without `paths:`, `@imports` | **Yes, full text** | Biggest usual culprit; also loads into most subagents. Keep CLAUDE.md under ~200 lines |
| Skills | Name + description only | Body loads when used. `disable-model-invocation: true` → zero until you run it |
| Subagent definitions | Name + description only | Their system prompt only loads when spawned |
| Plugins | Names + descriptions of everything they bundle | `claude plugin details <name>` shows the always-on figure |
| MCP servers | Tool names + instructions with tool search on; **every tool's full definition** with it off | Tool search is off by default behind a gateway (non-Anthropic `ANTHROPIC_BASE_URL`); `ENABLE_TOOL_SEARCH=true` restores it if the gateway forwards `tool_reference` blocks. A CLI like `gh`/`az` is still cheaper |
| Hooks | **No** | Only if the hook prints output back to Claude |

Built-in commands to use alongside the audit:
- `/context all` — real token breakdown for this session, including per-MCP-tool
- `/doctor` — flags unused plugins, skills, MCP servers; proposes CLAUDE.md trims
- `/skill-doctor` — skills that are listed but never invoked
- `/usage` — share of usage by skill, subagent, plugin, MCP server
- `/clear` between unrelated tickets (free); `/compact <what to keep>` when you need continuity

## Install

Copy `.claude/` into your repo root (or into `~/.claude/` to have it everywhere).
Create the ticket scratch folder and keep it out of git:

```bash
mkdir -p .claude/tickets .claude/audits
echo ".claude/tickets/" >> .gitignore
```

Commit `.claude/audits/` if you want the trend visible to the team; otherwise ignore it too.

## Context audit

```
/context-audit            # full audit
/context-audit plugins    # focus one area
```

It runs in a forked subagent, so the scanning doesn't land in your session — you get a
≤25-line summary back and the full report in `.claude/audits/context-<date>.md`. It never
edits config; you approve each fix. Each run compares against the previous report.

It runs in the background; the summary appears when it finishes.

**Running it periodically:** set a monthly calendar reminder and run it at the start of a
session. Scheduled tasks can't run a skill marked `disable-model-invocation: true`; to
schedule it, delete that line (its description then costs a few tokens per request).
Avoid `/loop` for this: it's session-only and re-sends your full context each time it fires.

## Ticket team

```bash
claude --agent ticket-lead
> Pick up ABC-123: <paste the ticket and its acceptance criteria>
```

| Agent | Model | Tools | Gets CLAUDE.md? | Why |
|---|---|---|---|---|
| ticket-lead | opus | Can only spawn the three below | yes | Planning and judgement; keeps its own context lean |
| ticket-scout | sonnet | Read-only + writes findings | **no** (`omitClaudeMd`) | Searching doesn't need conventions |
| ticket-implementer | sonnet | Edit/Write/Bash | yes | Needs conventions to write code |
| ticket-reviewer | opus | Read-only + writes review | yes | Independent check — never sees the implementer's reasoning |

How context stays small:
- Agents hand off through files in `.claude/tickets/<KEY>/` (`brief.md`, `findings.md`,
  `changes.md`, `review.md`), not by pasting content into prompts.
- Each agent returns ≤10–15 lines; the detail lives in its file.
- The `Agent(ticket-scout, ticket-implementer, ticket-reviewer)` allowlist only works because
  the lead runs as the main session via `--agent`.
- Rework resumes the existing implementer rather than spawning a fresh one, so it doesn't
  re-read everything.

Notes:
- `--agent` replaces Claude Code's default system prompt with the lead's, so keep it for
  ticket work.
- The lead resumes the implementer with `SendMessage`, which is why it's in its tools.
- `omitClaudeMd` needs Claude Code v2.1.271 or later; older versions ignore it (harmless).

Tuning: drop the lead or reviewer to `sonnet` if usage matters more than rigour. If a
workflow needs an MCP server (e.g. Jira), declare it inline in that agent's `mcpServers:`
and remove it from `.mcp.json` — then only that agent carries the tools.

## What to add to CLAUDE.md

Keep it to this — the details live in the files above, which load on demand:

```markdown
## Context hygiene
- For ticket work, use `claude --agent ticket-lead`; ticket notes live in `.claude/tickets/<KEY>/`.
- Delegate wide searches and verbose commands (full test runs, logs) to a subagent; return summaries only.
- When showing test/build output, filter to failures.
- Run /context-audit monthly or after adding a plugin, MCP server or skill.

# Compact instructions
Keep: current ticket key, acceptance criteria, files changed, failing tests. Drop: exploration output.
```
