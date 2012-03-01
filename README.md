# forthold.graphtag

This is a clojure utility application to integrate twitter to neo4j on heroku.

It monitors twitter mentions against a custom account "GraphTag" and inpute the content of those tweets into neo4j as a node.
The individual that made the tweet is also stored in neo4j and links are created from thier node to any tweets they mention "GraphTag" in.

The intention is to expand this to include a web front end and extra functionality sucah as a DSL i ntwitter to alow taging out tweets to categories -> a twitter graph organising function!

Libraries used are:

### Neocons - neo4j rest client
### twitter-api - clojure based twitter api wrapper
### Qiartzite - To wrap around quartz using clojure 
### Ring - Good old basic salt of the earth clojure web framework

## Usage

lein deps should not be necessary as I have included the lib folder as I have had to rush through some changes to the above libraries and have not yet forked etc to ensure they are avilalbel. This is not ideal I know but will fix asap.

```bash
lein run
```

## License

Copyright (C) 2011 Aran Elkington

Distributed under the Eclipse Public License, the same as Clojure.

