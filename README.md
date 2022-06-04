* https://github.com/utahkay/clojure-banking
* https://github.com/cordmata/seven/blob/master/clojure/src/clojure_seven/barber.clj
* https://github.com/cordmata/clojure-koans
* https://github.com/clojure/test.check
* https://stackoverflow.com/questions/4999281/ref-set-vs-commute-vs-alter
* https://stackoverflow.com/questions/48761023/clojures-commute-example-from-the-docs-produces-duplicates

## basics

## concurrency
* identity and state as separate things
    * state = value or set of values belonging to an identity
        * or in different words: the value of an identity at a particular point time
    * identity = series of states, separated by time
* analogy
    * person’s favorite set of movies
    * as a child: Disney and Pixar
    * grownup: Tim Burton or Robert Zemeckis
    * what changes over time?
        * not the set itself but which set the entity favorite-movies refers to
    * identity = favorite movies
        * subject of all the action in the associated program
    * state = sequence of favorite movies
        * sequence of values that this identity assumes over the course of the program
* clojure has four reference types to store state
    * Refs: coordinated, synchronous changes to shared state
    * Atoms: uncoordinated, synchronous changes to shared state
    * Agents: asynchronous changes to shared state
    * Vars: thread-local state
* mechanism provides a mutable container storing an immutable value
* reference types implement IRef
* table of functions
    |IRef   |create-fn    |update-fn(s)     |set-fn         |
    |---    |---          |---              |---            |
    |Atom   |atom         |swap!            |reset!         |
    |Ref    |ref          |alter, commute   |ref-set        |
    |Var    |def          |alter-var-root   |var-set        |
    |Agent  |agent        |send, send-off   |restart-agent  |
* summary
    * to create: `(create-fn container)`
    * to update: `(update-fn container data-fn & args)`
    * to assign a value: `(set-fn container new-val)`
    * get data: @ reader macro
* software transaction memory
    * refs are mutable, you must protect their updates
        ```
        (ref-set demo "new value")
        -> java.lang.IllegalStateException: No transaction running
        ```
    * transactions are wrapped in a `dosync`
        ```
        (dosync & exprs)
        ```
    * example
        ```
        (dosync (ref-set demo "new value"))
        -> "new value"
        ```
    * properties
        * like database transactions
        * allows programmers to describe reads and writes to stateful references in the scope
          of a transaction
        * atomic
            * if you change more than one ref in a single transaction, the changes are all
              coordinated to "happen at the same time" from the perspective of any code
              outside the transaction
        * consistent
            * refs can specify validation functions
        * isolated
            * transactions can’t see partially completed results from other transactions
        * transactions are in-memory transactions, Clojure does not guarantee that updates are durable
    * details
        * uses a technique called Multiversion Concurrency Control (MVCC)
        * steps
            1. Transaction A begins by taking a point, which is simply a number that acts
               as a unique timestamp in the STM world
                * Transaction A has access to its own
                  effectively private copy of any reference it needs, associated with the point
                    * persistent data structures make it cheap to provide these effectively private copies
            1. During Transaction A, operations on a ref work against (and return) the
               transaction’s private copy of the ref’s data, called the in-transaction value
            1. If at any point the STM detects that another transaction has already
               set/altered a ref that Transaction A wants to set/alter, Transaction A is forced
               to retry
            1. If and when Transaction A commits, its heretofore private writes will become
               visible to the world, associated with a single point in the transaction timeline.
    * alter vs commute
        * you probably want to use alter most of the time
        * commute is an optimized version of alter
            * if the STM is deciding if your transaction is safe to commit and has conflicts only
            on commute operations (none on alter operations) then it can commit without having to
            restart
            * example: incrementing the counter
        * the last in-transaction value you see from a commute will not always match the end-of-transaction
        value of a ref, because of reordering
            ```
            (def counter (ref 0))

            (defn commute-inc! [counter]
              (dosync (Thread/sleep 100) (commute counter inc)))

            (defn alter-inc! [counter]
                 (dosync (Thread/sleep 100) (alter counter inc)))

            (defn bombard-counter! [n f counter]
              (apply pcalls (repeat n #(f counter))))

            (defn -main [& args]
              (println (time (doall (bombard-counter! 20 commute-inc! counter)))) // compare with alter-inc!
              (println @counter)
              (shutdown-agents))
            ```
            results
            ```
            // with commute
            "Elapsed time: 226.6494 msecs"
            (9 6 1 3 3 3 1 3 3 1 3 12 12 16 18 12 12 12 20 12)
            20

            // with alter
            "Elapsed time: 2224.0148 msecs"
            (2 5 6 1 10 12 8 16 13 4 3 11 9 7 14 20 15 17 19 18)
            20
            ```
