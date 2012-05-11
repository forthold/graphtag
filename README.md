# forthold.graphtag

This is a clojure utility application to integrate twitter to neo4j on heroku.

It monitors twitter mentions against a custom account "GraphTag" and inpute the content of those tweets into neo4j as a node.
The individual that made the tweet is also stored in neo4j and links are created from their node to any tweets they mention "GraphTag" in.

As there is no front end at the moment the only way to view what is going on is to look at the neo4j console here: 

* WEB-Admin http://a9d1efcc4.hosted.neo4j.org:7062/webadmin/
* Login	0e5d0bb39
* Password	2d1a471df

Once I have a front end up you will be able to see you own personal tweet networks as they grow!

In the meantime if you want to know what node your mentions populate off just ping me on Twitter @forthold or run this cypher query in the web admin console:

start n = node:username(name = "forthold") return ID(n)

Once you have that you can put the id in the Data Browser tab and see your links and mentions.

The intention is to expand this to include a web front end and extra functionality such as a DSL in twitter to allow taging out tweets to categories leading towards a twitter graph based organising function!

Libraries used are:

* Neocons: written by neo4j rest client
* Quartzite - To wrap around quartz using clojure 
* twitter-api - clojure based twitter api wrapper
* Ring - Good old basic salt of the earth clojure web framework

## Usage

To run tests locally you will need to have a local neo4j server running and details put in start.sh.

You will also need to be on the internet for the twitter related tests to access the api.

```bash
lein deps
lein compile
./start.sh
```
## License

Copyright (C) 2011 Aran Elkington

Distributed under the Eclipse Public License, the same as Clojure.

