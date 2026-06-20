# AGENTS.md

## Quick commands
- Build + generate ANTLR: `make` (generates parser/lexer into `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/antlr/` and compiles to `build/`)
- Regenerate parser only: `make parser`
- Run compiler main: `make run` (executes `cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler.CompilerMain`)
- Clean (also deletes generated ANTLR sources): `make clean`

## Test harness
- `./main.sh <logfile>` runs the local grading loop: compiles, translates `mandrill-src/*.mds` to `data.asm`, runs simulator with `mandrill-in/*.in`, and diffs against `mandrill-ans/*.ans`.
- The script expects to run from repo root and uses `data.mds`, `mandrill.in`, `data.asm`, `data.out` in-place.

## Entrypoints and structure
- Compiler entry: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/CompilerMain.java`
- Simulator entry: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/simulator/SimulatorMain.java`
- ANTLR grammar: `Mandrill.g4` (generated files live under `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/antlr/` and are wiped by `make clean`).

## Notes
- The repo uses a hand-rolled `Makefile` and `antlr-4.13.2-complete.jar`; there is no Maven/Gradle build.
