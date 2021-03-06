(ns ^{:doc "Storage API for talking to external storage"
      :author "Paula Gearon"}
    naga.store)

(defprotocol Storage
  (start-tx [store] "Starts a transaction, if supported")
  (commit-tx [store] "Commits a transaction, if supported")
  (new-node [store] "Allocates a node for the store")
  (node-type? [store p n] "Returns true if the value refered to by a property can be a graph node")
  (data-property [store data] "Returns the property to use for given data. Must be in the naga namespace, and start with 'first'.")
  (container-property [store data] "Returns the property to use to indicate a containership relation for given data. Must be in the naga namespace")
  (resolve-pattern [store pattern] "Resolves a pattern against storage")
  (count-pattern [store pattern] "Counts the size of a pattern resolition against storage")
  (query [store output-patterns patterns] "Resolves a set of patterns (if not already resolved) and joins the results")
  (assert-data [store data] "Inserts new axioms")
  (query-insert [store assertion-patterns patterns] "Resolves a set of patterns, joins them, and inserts the set of resolutions"))

(defn retrieve-contents
  "Convenience function to retrieve the contents of the entire store"
  [store]
  (resolve-pattern store '[?entity ?attribute ?value]))

(def registered-stores (atom {}))

(defn register-storage!
  "Registers a new storage type"
  [store-id factory-fn]
  (swap! registered-stores assoc store-id factory-fn))

(defn get-storage-handle
  "Creates a store of the configured type. Throws an exception for unknown types."
  [{type :type store :store :as config}]
  (or store
      (if-let [factory (@registered-stores type)]
        (factory config)
        (throw (ex-info "Unknown storage configuration" config)))))
