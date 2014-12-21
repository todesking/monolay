# Monolay: Monospace Text Layout Engine for Scala
[![Circle CI](https://circleci.com/gh/todesking/monolay.png?style=badge)](https://circleci.com/gh/todesking/monolay)
[![Coverage Status](https://img.shields.io/coveralls/todesking/monolay.svg)](https://coveralls.io/r/todesking/monolay?branch=master)


Monolay is text layout engine.
This is useful to generate simple text format(like Markdown), or to build user-friendly console application.

```scala
val l = new com.todesking.monolay.Layout(40)

l.appendText("""Monolayは、Scalaで書かれた等幅テキストレイアウトエンジンです。コンソールアプリケーションの出力や、markdownのようなフ ォーマットの整形に適しています。""")

l.requireEmptyLines(1)

l.appendText("Monolay support automatic word-breaking, full-width characters, table layout.")

l.requireEmptyLines(1)

l.appendText("""Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.""")

l.requireEmptyLines(1)

l.renderTable {t => t.setHeader(Seq("S", "V", "O")); t.addRow(Seq("It", "support", "tables")) }

l.withIndent(10) {
  l.appendText("And indent control is supported.")
  l.renderTable {t => t.addRow(Seq("Combination", "is", "OK"))}
}

println(l.toString)
```

```
Monolayは、Scalaで書かれた等幅テキストレ
イアウトエンジンです。コンソールアプリケ
ーションの出力や、markdownのようなフォー
マットの整形に適しています。

Monolay support automatic word-breaking,
full-width characters, table layout.

Lorem ipsum dolor sit amet, consectetur
adipiscing elit, sed do eiusmod tempor
incididunt ut labore et dolore magna
aliqua. Ut enim ad minim veniam, quis
nostrud exercitation ullamco laboris
nisi ut aliquip ex ea commodo consequat.
Duis aute irure dolor in reprehenderit
in voluptate velit esse cillum dolore eu
fugiat nulla pariatur. Excepteur sint
occaecat cupidatat non proident, sunt in
culpa qui officia deserunt mollit anim
id est laborum.

+----+---------+--------+
| S  | V       | O      |
+====+=========+========+
| It | support | tables |
+----+---------+--------+
          And indent control is
          supported.
          +-------------+----+----+
          | Combination | is | OK |
          +-------------+----+----+
```

## Features

* Text layout engine
  * Word breaking
  * Text alignment control(currently, align left and equal spaced is supported)
  * Vertical alignment control("require N empty lines above the content")
  * Indentation
  * Full-width characters(ambiwidth is not yet)
* Table layout engine
  * Auto width calculation
  * Omit text longer than column width
  * Multi-line columns

## Current status

STATUS: Under construction.

## Related products

There's some table layout library for Java.

* [j-text-utils](https://code.google.com/p/j-text-utils/)
* [Java Text Table Formatter](http://texttablefmt.sourceforge.net/)
* [java-ascii-table](https://code.google.com/p/java-ascii-table/)
* [MultiColumnPrinter.java](http://www.java2s.com/Code/Java/Development-Class/Utilityclassforprintingalignedcolumnsoftext.htm)
  * I don't know where the class come from...

