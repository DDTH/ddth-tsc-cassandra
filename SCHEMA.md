ddth-tsc-cassandra Table Schema
===============================

## History ##

#### 2014-03-12 ####
First release

## Keyspace Schema ##

```sql
CREATE KEYSPACE keyspace_name
WITH replication={'class':'SimpleStrategy','replication_factor':'1'}
 AND durable_writes=true;
```


## Counter Table Schema ##

```sql
CREATE TABLE "keyspace_name"."tsc_counters" (
    c        varchar,
    ym       int,
    d        int,
    t        bigint,
    v        counter,
    PRIMARY KEY ((c, ym, d), t)
) WITH COMPACT STORAGE;
```

CREATE TABLE table_name (
    rrow_key        varchar,
    col_key       varchar,
    col_val        varchar
    PRIMARY KEY (rrow_key, col_key)
) WITH COMPACT STORAGE;