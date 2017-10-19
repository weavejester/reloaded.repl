# reloaded.repl

A Clojure library that implements the user functions of Stuart
Sierra's [reloaded workflow][1].

This library will save you from having to write out the same reloaded
functions in your user.clj file for each project. It'll also ensure
you don't lose your `reset` function every time your source code has a
compilation error.

Your application must use the [Component][2] library, and provide
[idempotent][3] start and stop functions for your system.

[1]: http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded
[2]: https://github.com/stuartsierra/component
[3]: https://en.wikipedia.org/wiki/Idempotence

## Install

Add the following dependency to your dev profile:

    [reloaded.repl "0.2.4"]

## Usage

Require the `reloaded.repl` namespace in your user.clj file, and use
the `set-init!` function to define a function that initializes your
top-level system.

For example:

```clojure
(ns user
  (:require [reloaded.repl :refer [system init start stop go reset reset-all]]
            [your-app.system :refer [new-system]]))

(reloaded.repl/set-init! #(new-system {:port 3000}))
```

## License

Copyright © 2017 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
