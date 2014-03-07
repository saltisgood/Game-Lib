Android Game Library
===============
The Android Game Library is a general library for OpenGL and related features primarily useful for creating games.

Features
===============
The library is mainly focused at the moment with OpenGL helpers so that using those components are just as easy (and with very similar syntax) as using the View subclasses in the Android framework, of which the equivalent in this library is the Shape class.
All subclasses of Shape support standard Android touch behaviour, such as clicks and long clicks and Containers (the equivalent of ViewGroups) also fully support scrolling in 2D.
The library also has a significant abstraction for drawing text in OpenGL, which is normally a pretty arduous process.

Status
===============
This library is still very early in development so expect things to be refactored and changed significantly at various times. I would recommend not using it yet unless you just want a starting point for your own OpenGL implementations.

Dependencies
===============
This library has a dependency on [my other android library](https://github.com/saltisgood/salt-android-library), also hosted on GitHub.