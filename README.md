# Rosetta — Vertical Slice (0.2.0)

This version adds a working ingestion pipeline and two CLI commands:

- `analyze-repo <gitUrl>` — clones to `./work/<repoName>` (or updates if exists), walks files,
  detects language by extension, computes stub metrics (LOC + naive complexity), and persists results.
- `list-metrics` — prints file path, metric key, value.

## Build
mvn -q -DskipTests package

## Run examples
java -jar rosetta-cli/target/rosetta-cli-0.2.0-SNAPSHOT-jar-with-dependencies.jar analyze-repo https://github.com/eclipse/jgit.git
java -jar rosetta-cli/target/rosetta-cli-0.2.0-SNAPSHOT-jar-with-dependencies.jar list-metrics

DB: ./data/rosetta.db (created automatically).
Workdir for repos: ./work/
