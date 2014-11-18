package com.todesking.monolay

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.Assertions._

class LayoutSpec extends FunSpec with Matchers {
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
        it("should return contents of lines, no last line delimiter") {
          val subject = new Layout(80)
          subject.appendRaw("a")
          subject.newLine()
          subject.appendRaw("b")
          subject.newLine()

          subject.toString() shouldEqual "a\nb"
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
    describe("#appendUnbreakable") {
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
  }
}
