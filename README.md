# Monolay: Monospace Text Layout Engine for Scala
[![Circle CI](https://circleci.com/gh/todesking/monolay.png?style=badge)](https://circleci.com/gh/todesking/monolay)
[![Coverage Status](https://img.shields.io/coveralls/todesking/monolay.svg)](https://coveralls.io/r/todesking/monolay?branch=master)


Monolay is text layout engine.
This is useful to generate simple text format(like Markdown), or to build user-friendly console application.

## Features

* Text layout engine
  * Word breaking
  * Text alignment control(currently, align left and equal spaced is supported)
  * Vertical alignment control("require N empty lines above the content")
  * Indentation
* Table layout engine
  * Auto width calculation
  * Omit text longer than column width
  * Multi-line columns
  * Unicode support

## Current status

STATUS: Under construction.

The project based on these classes:

* [dox's Layout class](https://github.com/todesking/dox/blob/6dfdb522fb3d5c7040dc64274e5e9cad0b8635ba/formatter.scala#L3)
* [jcon's Table class](https://github.com/todesking/jcon/blob/04d7ab11f13959d2b6f579695112bd85bce6e768/src/main/scala/Table.scala)

I'll porting these, and write tests.

## Related products

There's some table layout library for Java.

* [j-text-utils](https://code.google.com/p/j-text-utils/)
* [Java Text Table Formatter](http://texttablefmt.sourceforge.net/)
* [java-ascii-table](https://code.google.com/p/java-ascii-table/)
* [MultiColumnPrinter.java](http://www.java2s.com/Code/Java/Development-Class/Utilityclassforprintingalignedcolumnsoftext.htm)
  * I don't know where the class come from...

