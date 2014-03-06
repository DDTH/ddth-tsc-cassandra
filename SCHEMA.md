ddth-tsc-cassandra Table Schema
===============================

## History ##

#### 2014-03-07 ####
First release

## Keyspace Schema ##

```sql
CREATE KEYSPACE keyspace_name
WITH replication={'class':'SimpleStrategy','replication_factor':'1'}
 AND durable_writes=true;
```


## Counter Table Schema ##

```sql
CREATE TABLE "keyspace_name"."table_name" (
    c        varchar,
    ym       int,
    d        int,
    t        bigint,
    v        counter,
    PRIMARY KEY ((c, ym, d), t)
) WITH COMPACT STORAGE;
```