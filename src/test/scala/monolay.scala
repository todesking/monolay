package com.todesking.monolay

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.Assertions._

class LayoutSpec extends FunSpec with Matchers {
  def heredoc(s: String) =
    s.stripMargin.replaceAll("""\A\n|\n +\Z""", "")
  describe("Layout") {
    describe("#toString()") {
      describe("when empty") {
        it("should return empty string") {
          val subject = new Layout(80)
          subject.toString() shouldEqual ""
        }
      }
      describe("when contains only one(current) line") {
        it("should return the line content without line delimiter") {
          val subject = new Layout(10)
          subject.appendRaw("a")
          subject.toString() shouldEqual "a"
        }
      }
      describe("when contains some lines but current line is empty") {
        it("should return contents of lines") {
          val subject = new Layout(80)
          subject.appendRaw("a")
          subject.newLine()
          subject.appendRaw("b")
          subject.newLine()

          subject.toString() shouldEqual "a\nb\n"
        }
      }
      describe("when contains a line but current line is empty") {
        it("should return a empty line") {
          val subject = new Layout(80)
          subject.newLine()
          subject.toString() shouldEqual "\n"
        }
      }
      describe("when contains some lines and non-empty current line") {
        it("should return lines + current line without last line delimiter") {
          val subject = new Layout(80)
          subject.appendRaw("a")
          subject.newLine()
          subject.appendRaw("b")
          subject.newLine()
          subject.appendRaw("c")

          subject.toString() shouldEqual "a\nb\nc"
        }
      }
    }
    describe("#appendRaw()") {
      it("should append string to currentline as raw") {
        val subject = new Layout(10)
        subject.appendRaw("a" * 20)
        subject.appendRaw("b" * 20)
        subject.toString shouldEqual ("a" * 20 + "b" * 20)
      }
    }
    describe("#appendUnbreakable()") {
      describe("current line is empty") {
        it("should append non-breakable text fragment") {
          val subject = new Layout(5)
          subject.appendUnbreakable("a " * 10)
          subject.toString() shouldEqual ("a " * 10)
        }
      }
      describe("current line is not empty") {
        it("should append non-breakable text on next line") {
          val subject = new Layout(5)
          subject.appendRaw("111")
          subject.appendUnbreakable("a " * 10)
          subject.toString() shouldEqual ("111\n" + "a " * 10)
        }
      }
      it("should append multi lines at once") {
        val subject = new Layout(3)
        subject.appendUnbreakable("aaa\nbbbbbb\ncccc")
        subject.toString() shouldEqual "aaa\nbbbbbb\ncccc"
      }
    }
    describe("#appendText()") {
      it("should append text with word-breaking") {
        val subject = new Layout(5)
        subject.appendText("This is an apple.")
        subject.toString() shouldEqual "This\nis an\napple."
      }
      it("should append text with spacing") {
        val subject = new Layout(5)
        subject.appendRaw("a")
        subject.appendText("b")
        subject.toString() shouldEqual "a b"
      }
      it("should append text after optimalWidth if there is special character") {
        val subject = new Layout(3)
        subject.appendText("aaa")
        subject.appendText(".")
        subject.toString() shouldEqual "aaa."
      }
      it("should append text without spacing if cancelSpacing() called") {
        val subject = new Layout(5)
        subject.appendRaw("a")
        subject.cancelSpacing()
        subject.appendText("b")
        subject.appendText("c")
        subject.toString() shouldEqual "ab c"
      }
    }
    describe("#appendEqualSpaced()") {
      it("should add multiple string part as equal spaced layout") {
        val subject = new Layout(7)
        subject.appendEqualSpaced("a", "b", "c")
        subject.toString() shouldEqual "a  b  c"
      }
      describe("when no parts provided") {
        it("should just add empty line") {
          val subject = new Layout(7)
          subject.appendEqualSpaced()
          subject.toString() shouldEqual "\n"
        }
      }
    }
    describe("#terminateLine()") {
      describe("when current line is empty") {
        it("should do nothing") {
          val subject = new Layout(7)
          subject.terminateLine()
          subject.appendRaw("aaa")
          subject.toString() shouldEqual "aaa"
        }
      }
      describe("when current line is not empty") {
        it("should append line delimiter") {
          val subject = new Layout(7)
          subject.appendRaw("aaa")
          subject.terminateLine()
          subject.toString() shouldEqual "aaa\n"
        }
      }
    }
    describe("#cancelTerminateLine()") {
      it("should cancel terminateLine() effect") {
        val subject = new Layout(7)
        subject.toString() shouldEqual ""

        subject.cancelTerminateLine()
        subject.toString() shouldEqual ""

        subject.appendRaw("aaa")
        subject.terminateLine()
        subject.toString() shouldEqual "aaa\n"

        subject.cancelTerminateLine()
        subject.toString() shouldEqual "aaa"

        subject.cancelTerminateLine()
        subject.toString() shouldEqual "aaa"
      }
    }
    describe("#newLine()") {
      it("should append line delimiter") {
        val subject = new Layout(7)
        subject.newLine()
        subject.appendRaw("1")
        subject.newLine()
        subject.toString() shouldEqual "\n1\n"
      }
    }
    describe("#requireEmptyLines()") {
      it("should append empty lines to satisfy requirement") {
        val subject = new Layout(7)
        subject.requireEmptyLines(1)
        subject.toString() shouldEqual "\n"

        subject.requireEmptyLines(3)
        subject.toString() shouldEqual "\n\n\n"

        subject.requireEmptyLines(3)
        subject.toString() shouldEqual "\n\n\n"
      }
    }
    describe("#indent()") {
      it("should increment indent level") {
        val subject = new Layout(7)
        subject.appendRaw("a")
        subject.terminateLine()

        subject.indent(2)

        subject.appendRaw("b")
        subject.terminateLine()

        subject.appendRaw("c")
        subject.terminateLine()

        subject.indent(-1)
        subject.appendRaw("d")

        subject.toString() shouldEqual "a\n  b\n  c\n d"
      }
    }
    describe("#withIndent()") {
      it("should modify indent level within block") {
        val subject = new Layout(7)
        subject.appendRaw("a")
        subject.terminateLine()

        subject.withIndent(2) {
          subject.appendRaw("b")
        }
        subject.terminateLine()

        subject.appendRaw("c")

        subject.toString() shouldEqual "a\n  b\nc"
      }
    }
    describe("Usage") {
      val subject = new Layout(40)

      // reference: http://www.lipsum.com
      subject.appendUnbreakable("# The standard Lorem Ipsum passage, used since the 1500s #")

      subject.requireEmptyLines(1)

      subject.withIndent(2) {
        subject.appendText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
      }

      subject.toString() shouldEqual heredoc("""
       |# The standard Lorem Ipsum passage, used since the 1500s #
       |
       |  Lorem ipsum dolor sit amet,
       |  consectetur adipiscing elit, sed do
       |  eiusmod tempor incididunt ut labore et
       |  dolore magna aliqua. Ut enim ad minim
       |  veniam, quis nostrud exercitation
       |  ullamco laboris nisi ut aliquip ex ea
       |  commodo consequat. Duis aute irure
       |  dolor in reprehenderit in voluptate
       |  velit esse cillum dolore eu fugiat
       |  nulla pariatur. Excepteur sint
       |  occaecat cupidatat non proident, sunt
       |  in culpa qui officia deserunt mollit
       |  anim id est laborum.
      """)
    }
    describe("Tables") {
      describe("Example") {
        describe("Simple case") {
          val subject = new Layout(40)
          subject.renderTable { t =>
            t.setHeader(Seq("a", "b", "c"))
            t.addRow(Seq("1", "2", "foo"))
          }

          subject.toString shouldEqual heredoc("""
          |+---+---+-----+
          || a | b | c   |
          |+===+===+=====+
          || 1 | 2 | foo |
          |+---+---+-----+
          |
          """)
        }
        describe("Text overflow") {
          val subject = new Layout(100)
          subject.renderTable { t =>
            t.setWidths(Seq(10))
            t.setHeader(Seq("a"))
            t.addRow(Seq("a" * 20))
          }

          subject.toString shouldEqual heredoc(s"""
          |+------------+
          || a          |
          |+============+
          || aaaaaaa... |
          |+------------+
          |
          """)
        }
        describe("Without header") {
          val subject = new Layout(100)
          subject.renderTable { t =>
            t.addRow(Seq("aaa"))
          }

          subject.toString shouldEqual heredoc(s"""
          |+-----+
          || aaa |
          |+-----+
          |
          """)
        }
        describe("Only header") {
          val subject = new Layout(100)
          subject.renderTable { t =>
            t.setHeader(Seq("aaa"))
          }

          subject.toString shouldEqual heredoc(s"""
          |+-----+
          || aaa |
          |+=====+
          |+-----+
          |
          """)
        }
        describe("Empty") {
          val subject = new Layout(100)
          subject.renderTable { t => }

          subject.toString shouldEqual ""
        }
      }
    }
  }
}
