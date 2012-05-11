(ns forthold.graphtag.test-direct
  (:use  [clojure.test]
         [clojure.pprint]
         [twitter.oauth]
         [twitter.callbacks]
         [twitter.callbacks.handlers]
         [twitter.api.restful]
         [forthold.graphtag.common]
         [forthold.graphtag.followers])
  (:import [twitter.callbacks.protocols SyncSingleCallback]))


