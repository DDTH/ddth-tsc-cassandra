ddth-tsc-cassandra
==================

DDTH's Cassandra-backed Time Series Counter.

Project home:
[https://github.com/DDTH/ddth-tsc-cassandra](https://github.com/DDTH/ddth-tsc-cassandra)

OSGi environment: ddth-tsc-cassandra is packaged as an OSGi bundle.

See also [ddth-tsc](https://github.com/DDTH/ddth-tsc).

See also [Cassandra table schema](SCHEMA.md).


## License ##

See LICENSE.txt for details. Copyright (c) 2014 Thanh Ba Nguyen.

Third party libraries are distributed under their own licenses.


## Maven Release #

Latest release version: `0.3.2`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

Maven dependency:

```xml
<dependency>
	<groupId>com.github.ddth</groupId>
	<artifactId>ddth-tsc</artifactId>
	<version>0.3.2</version>
</dependency>
<dependency>
	<groupId>com.github.ddth</groupId>
	<artifactId>ddth-tsc-cassandra</artifactId>
	<version>0.3.2</version>
</dependency>
```


## Usage ##

Sample code:

```java
//first: we need a counter factory
CassandraCounterFactory counterFactory = new CassandraCounterFactory();
counterFactory
    .setHost("localhost")
    .setPort(9042)
    .setKeyspace("mykeyspace")
    .setTableTemplate("counter_tablename_template");
counterFactory.init();

//second: we need the counter
ICounter countSiteVisits = counterFactory.getCounter("my-site-visits");

//third: count!
  //there is one visit to my site
counterSiteVisits.add(1);

  //at a specific time, there are 3 visits to my site
counterSiteVisits.add(unixTimestampMs, 3);

//last: get the data out
long timestampLastHour = System.currentTimeMillis() - 3600000;
DataPoint[] lastHour = counterSiteVisits.get(timestampLastHour);

long timestampLast15Mins = System.currentTimeMillis() - 15*60*1000; //15 mins = 900000 ms
DataPoint[] last15MinsGroupPerMin = counterSiteVisits.get(timestampLast15Mins, 15*60); //1 min = 60 secs

//destroy the counter factory when done
counterFactory.destroy();
```

See also [ddth-tsc](https://github.com/DDTH/ddth-tsc).