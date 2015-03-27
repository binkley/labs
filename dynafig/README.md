Dynafig
=======

Example microproject demonstrating dynamically updated key-value pairs.

(NB - this file is GitHub-flavored MarkDown.  The IntelliJ plugin does not
correctly render though _class_ links in Preview do work, _method_ links do
not.)

TODO
----

1. Javadoc.
2. Default wiring implementations with common remote stores, e.g., JSR107 or
Cassandra.
3. Spring:
  * Factory for `Optional<Atomic*>`
  * Connect `@Inject @Named(key)` to `track*`
4. Cloud environment source for non-git.

Interfaces
----------

* [`Tracking`](dynafig-core/src/main/java/lab/dynafig/Tracking.java) is a
factory for key-value pairs.  The tracking calls take a `String` key name and
return a non-`null` `Optional<Atomic*>` (`*` depending on return type).  The
optional is empty if the key is missing.

   This means typical use looks like, e.g.:
   ```private final AtomicInteger rapidity;
   
   public MerryGoRound(@Nonnull final Dynafig settings) {
       rapidity = settings.trackInt("app.rapidity").
           orElseThrow(() -> new IllegalStateException(
                   "No property for 'app.rapidity'"));
   }
   
   public void spin() {
       final int rapidity = this.rapidity.get();
       // Use rapidity
   }```

   The four tracking choices are:
  * [`track`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L55)
  tracks plain string values returning `Optional<AtomicReference<String>>`
  * [`trackBool`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L66)
  tracks boolean values returning `Optional<AtomicBoolean>`
  * [`trackInt`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L77)
  tracks int values returning `Optional<AtomicInteger>`
  * [`trackAs`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L94)
  tracks values of type `T` given a conversion function, returning
  `Optional<AtomicReference<T>>`

* [`Updating`](dynafig-core/src/main/java/lab/dynafig/Updating.java) updates
pair values
  * [`update(key,value)`](dynafig-core/src/main/java/lab/dynafig/Updating.java#L23)
  updates a pair value or creates a new key-value pair if `key` is undefined
  * [`update(entry)`](dynafig-core/src/main/java/lab/dynafig/Updating.java#L33)
  is a convenience to update from a map entry
  * [`updateAll`](dynafig-core/src/main/java/lab/dynafig/Updating.java#L45)
  is a convenience to update a set of key-value in one call

Default implementation
----------------------

* [`Default`](dynafig-core/src/main/java/lab/dynafig/Default.java) is a
default implementation of `Tracking` and `Updating`,
[`DefaultTest`](dynafig-core/src/test/java/lab/dynafig/DefaultTest.java)
tests it
