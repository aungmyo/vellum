This article is part of the [Postgrest](Postgrest.md) series :)

## Introduction ##

```sql

command3_reindexConcurrently() { # db schema table
db=$1
schema=$2
table=$3
pg_dump $db -n  $schema -s -c |
grep "^CREATE INDEX idx_.* ON $table " |
sed -s 's/CREATE INDEX idx_\([_a-z]*\) ON \(.*\)/\nCREATE INDEX CONCURRENTLY idx2_\1 ON \2\nDROP INDEX idx_\1;\nALTER INDEX idx2_\1 RENAME TO idx_\1;/'
}
```

## Conclusion ##

### Resources ###

See the [Postgrest](Postgrest.md) page.

