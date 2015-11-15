Change Log
==========

Version 1.2.1
-------------

  * Added `mutable` attribute to `@Decor` to allow for mutable decorated objeect.

Version 1.2.0
-------------

  * Added `@Factory` annotation to generate an interface that create instance of the marked interface.

Version 1.1.1
-------------

  * FIX: code generated in library noopetal-annotation is now back to the intended 1.7 (was 1.8 preventing it working on Android).

Version 1.1.0
-------------

  * Both `@Noop` and `@Decor` support specifying the name of the generated class as the optional value.

Version 1.0.0
-------------

  * Added `@Decor` annotation to generate a decorator implementation of the marked interface.


Version 0.1.0 *(unpublished)*
----------------------------

Initial release.

 * `@Noop` annotation to generate a No-operation implementation of the marked interface.


Things to explore:

 * Allow the generated class to be an inner class to the interface.
 * Make that the default.
