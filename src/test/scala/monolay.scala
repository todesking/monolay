package com.todesking.monolay

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.Assertions._

class LayoutSpec extends FunSpec with Matchers {
  def withSubject[A](subject: A)(f: A => Unit) = f(subject)

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
  }
}
