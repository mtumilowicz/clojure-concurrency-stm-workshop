[![Build Status](https://app.travis-ci.com/mtumilowicz/clojure-concurrency-stm-workshop.svg?branch=master)](https://app.travis-ci.com/mtumilowicz/clojure-concurrency-stm-workshop)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# clojure-concurrency-stm-workshop
* references
    * https://github.com/utahkay/clojure-banking
    * https://github.com/cordmata/seven/blob/master/clojure/src/clojure_seven/barber.clj
    * https://github.com/cordmata/clojure-koans
    * https://github.com/clojure/test.check
    * https://stackoverflow.com/questions/4999281/ref-set-vs-commute-vs-alter
    * https://stackoverflow.com/questions/48761023/clojures-commute-example-from-the-docs-produces-duplicates
    * http://makble.com/whats-the-difference-between-alter-and-commute-in-clojure-ref-type
    * https://clojure.org/
    * https://www.manning.com/books/clojure-in-action
    * https://pragprog.com/titles/dswdcloj3/web-development-with-clojure-third-edition/
    * https://pragprog.com/titles/shcloj3/programming-clojure-third-edition/
    * https://www.manning.com/books/the-joy-of-clojure-second-edition
    * https://pragprog.com/titles/vmclojeco/clojure-applied/

## preface
* goals of this workshop:
    * introduction into clojure
    * basics about clojure syntax
    * showcase of concurrency capabilities of clojure: ref, atom & agent
    * understanding how software transactional memory works
    * introduction to organizing code with namespaces
    * show how to facilitate development using REPL
* workshops
    * refactor core_atom and fix test
    * implement sleeping barber solution

## clojure
* functional programming language on the JVM with great support for managing state and concurrency
* based on two fundamental tools: immutable values and pure functions
* syntax derived from its Lisp roots
    * Lisps have a tiny language core, almost no syntax, and a powerful macro facility

## syntax
* parentheses serve two purposes:
    * calling functions
        * Clojure assumes that the first symbol appearing in a list represents the name of a function (or a macro)
        * remaining expressions in the list are considered arguments to the function
    * constructing lists
        * at the meta level, your entire Clojure program is a series of lists
        * program is interpreted by the Clojure compiler as lists that
        contain function names and arguments that need to be parsed, evaluated, etc
        * this enables uniquely powerful meta-programming capabilities
            * Clojure code is represented using Clojure data structures
        * list is special because each expression of Clojure code is a list
            * (def three-numbers (1 2 3))
                * CompilerException java.lang.ClassCastException: java.lang.Long cannot be cast to clojure.lang.IFn
                * Clojure is trying to treat the list (1 2 3) the same way as it treats all lists
                    * first element: a function and 1 is not a function
            * (def three-numbers '(1 2 3)) // quoting
* symbols and keywords
    * symbols = identifiers in a Clojure program (names that signify values)
        * example
            * (+ 1 2)
            * + is a symbol signifying the addition function
        * analogy
            * the word in a dictionary entry is the symbol but the definition of the word
            is a binding of that word to a particular meaning
    * symbols resolve to something else that isn’t a symbol
    * keyword = never reference some other value and always evaluate to themselves
        * often used as keys in maps and they provide faster comparisons and lower memory overhead than strings
        (because instances are cached and reused)
    * you can construct keywords and symbols from strings using the keyword and symbol functions
* defining function: `(defn addition-function [x y] (+ x y))`
    * optional arguments: `(defn fn-with-opts [f1 f2 & opts] ,,, )`
        * similar to varargs in java
        * example
            ```
            (defn total-all-numbers [& numbers]
              (apply + numbers))
            ```
    * overloading
        ```
        (defn total-cost
            ([item-cost number-of-items] (* item-cost number-of-items))
            ([item-cost] (total-cost item-cost 1)))
        ```
* let
    * allows you to introduce locally named things
    * binds a symbol to a value
    * example: `(let [a 1 b 2] (+ a b))` // 3
* exceptions
    ```
    (try
        (throw
            (ex-info "The ice cream has melted!"
               {:causes             #{:fridge-door-open :dangerously-high-temperature}
                :current-temperature {:value 25 :unit :celcius}}))
    ```
    or
    ```
    (throw (Exception. "this is an error!"))
    ```
* nil
    * nil has the same value as Java null
    * everything other than false and nil is considered true
* loop / recur
    * Clojure doesn’t have traditional for loops
    * example
        ```
        (defn fact-loop [n]
            (loop [current n fact 1] // exactly as let
            (if (= current 1)
                fact
                (recur (dec current) (* fact current)))))
        ```
    * loop works like let: establishing bindings and then evaluating exprs
        * loop sets a recursion point, which can then be targeted by the recur
    * recur binds new values for loop ’s bindings and returns control to the top of the loop
    * instead of using a loop, you can recur back to the top of a function
        ```
        (defn countdown [result x]
            (if (zero? x)
                result
                (recur (conj result x) (dec x))))
        ```
    * list comprehension
        ```
        (for [x [0 1 2 3 4 5]
              :let [triple (* x 3)] // map -> 0 3 6 9 12 15
              :let [double (* triple 2)] // map -> 0 6 12 18 24 30
              :when (even? double)] // filter - only even
          double)
        ```
    * and a lot of standard methods: map, filter, reduce
* useful higher order functions
    * (apply + list-of-expenses)
        ```
        (max [1 2 3]) // returns [1 2 3]

        (apply max [1 2 3]) // 3, same as (max 1 2 3)
        ```
    * partial
        ```
        (def hundred-times (partial * 100))
        (hundred-times 5) // 500
        ```

## collections
* simplest way to model data is to use Map
    * in Clojure: change is always modeled as the application of a pure function to an immutable value,
    resulting in a new immutable value
    * (assoc {} :name "Michal" :age 30) // adds
    * (dissoc {:a 1 :b 2 :c 3} :b) // removes
    * (update person :age inc) // modifies
    * get from map
        * (get earth :name), (get earth :name "default name")
        * (earth :name) // invoking the map
        * (:name earth), (:name earth "default name")  // invoking the keyword key
            * preferred method
    * working with nested maps
        * (assoc-in users [:kyle :summary :average :monthly] 3000) // new entry
            * if any nested map doesn’t exist along the way, it gets created and correctly associated
        * (get-in users [:kyle :summary :average :monthly])
        * (update-in users [:kyle :summary :average :monthly] + 500)
* other collections
    * list
    * vector: [10 20 30 40 50]
        * indexed by number
    * set
    * operations
        * conj add elements at the natural insertion point
            * lists: at the beginning
            * vectors: at the end
        * bulk updates
            * call transient to get a mutable version of a collection
            * transient collections can’t be conj and assoc
                * equivalent set of functions that mutate the instance, all with a ! suffix
                    * conj! , assoc!
            * when mutation is complete, call persistent! to get back to a persistent collection

## namespaces
* namespaces provide a means to organize our code and the names we use in our code
* is both a name context and a container for vars
    * vars are associations between a name (a symbol) and a value
    * vars are created using def and other special forms or macros that start with def, like defn
    * all vars are globally accessible via their fully-qualified name
* convention: namespace names are typically lower-case and use - to separate words
* Clojure runtime tracks the current namespace in the var clojure.core/*ns*

## repl
* current namespace: `*ns*`
* switch to existing namespace: `in-ns`
    * example: `(in-ns 'workshop.barber.core)`
* if the namespace is not used in the main class of the project (directly or indirectly), it won't
be accessible without first requiring it
    ```
    (require '[workshop.barber.core :as barber])
    barber/max-chairs
    (in-ns 'workshop.barber.core)
    ```

## leiningen
* offers various project-related tasks and can:
    * create new projects
    * fetch dependencies for your project
    * run tests
    * run a fully-configured REPL
    * run the project
    * compile and package projects for deployment
    * run custom automation tasks written in Clojure (leiningen plug-ins)
* Leiningen could be thought of as "Maven meets Ant without the pain"
* project
    * a directory containing a group of Clojure source files
    * metadata about them
        * project name
        * project description
        * dependencies
        * Clojure version
        * main namespace of the app
* Clojure libraries are distributed the same way as in other JVM languages: as jar files
    * Leiningen by default uses
        * clojars.org (Clojure community's centralized Maven repository)
        * Maven Central
* extra arguments to the JVM: `:jvm-opts` vector
* useful commands
    * lein run
    * lein test
    * lein repl

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
        * example for commit order
            ```
            (defn tid [] (.getId (Thread/currentThread)))
            (defn debug
              [ msg ]
              (print (str msg (apply str (repeat (- 35 (count msg)) \space))  " tid: " (tid)  "\n"))
              (flush))

            (def a (ref 1))

            (defn do-transaction [_]
              (dosync
                (alter a + 1) // or commute a + 1
                (debug (str "in transaction value: " @a))
              )
            )

            (doseq [dummyagent (range 1 6)]
              (send-off (agent  dummyagent) do-transaction)
            )
            ```
            results
            ```
            in transaction value: 2             tid: 12
            in transaction value: 3             tid: 15 // failed, will be retried
            in transaction value: 3             tid: 14 // failed, will be retried
            in transaction value: 3             tid: 11
            in transaction value: 4             tid: 15 // retried and succeed
            in transaction value: 5             tid: 13
            in transaction value: 6             tid: 14 // retried and succeed
            ```