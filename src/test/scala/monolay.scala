package com.todesking.monolay

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.Assertions._

class LayoutSpec extends FunSpec with Matchers {
  describe("Layout") {
    describe("when empty") {
      def subject() = new Layout(80)

      describe("#toString()") {
        it("should return empty string") {
          subject().toString() shouldEqual ""
        }
      }

    }

  }
}
