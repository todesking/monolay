package com.todesking.monolay

class Layout(optimalWidth:Int, private var indentLevel:Int = 0) {
  import scala.collection.mutable
  private var lines = mutable.ArrayBuffer.empty[String]
  private var currentLine:String = ""
  private var cancelNextSpacing = false

  def cancelSpacing():Unit = {
    cancelNextSpacing = true
    currentLine = currentLine.replaceAll("""\s+\z""", "")
  }

  def cancelTerminateLine(): Unit =
    if(!hasCurrentLineContent && lines.lastOption.nonEmpty) {
      currentLine = lines.last
      lines.remove(lines.size - 1)
    }

  def requireEmptyLines(n:Int):Unit = {
    terminateLine()
    val emptyLines = lines.reverse.takeWhile(_.isEmpty).size
    0 until ((n - emptyLines) max 0) foreach { _=> newLine() }
  }

  def restWidth:Int = optimalWidth - indentLevel

  override def toString() =
    (lines.nonEmpty, hasCurrentLineContent) match {
      case (false, false) =>
        ""
      case (false, true) =>
        currentLine
      case (true, false) =>
        lines.mkString("\n") + "\n"
      case (true, true) =>
        lines.mkString("\n") + "\n" + currentLine
    }

  def appendRaw(str: String): Unit =
    currentLine += str

  def appendText(str:String):Unit =
    doMultiLine(str)(appendBreakable0)

  def appendUnbreakable(str:String):Unit =
    doMultiLine(str)(appendUnbreakable0(_, needSpacing = true))

  def appendEqualSpaced(parts:String*):Unit = {
    parts.size match {
      case 0 => newLine()
      case 1 =>
        val pad = " " * ((restWidth - width(parts(0))) / 2).max(0)
        appendUnbreakable(pad + parts(0))
      case _ =>
        val pad = " " * ((restWidth - parts.map(width(_)).sum) / (parts.size - 1)).max(1)
        appendUnbreakable(parts.mkString(pad))
    }
  }

  private[this] def doMultiLine(str:String)(f:String => Unit):Unit = {
    val lines = str.split("\n")
    assert(lines.nonEmpty)
    lines.dropRight(1).foreach {line =>
      f(line)
      newLine()
    }
    f(lines.last)
  }

  private[this] def hasCurrentLineContent():Boolean =
    currentLine.nonEmpty && currentLine != " " * indentLevel

  def indent(n:Int):Unit = {
    require(indentLevel + n >= 0)
    if(!hasCurrentLineContent) {
      indentLevel += n
      currentLine = " " * indentLevel
    } else {
      indentLevel += n
    }
  }

  def withIndent(n:Int)(f: =>Unit):Unit = {
    indent(n)
    f
    indent(-n)
  }

  def terminateLine():Unit = {
    if(hasCurrentLineContent) {
      newLine()
    }
  }

  def newLine():Unit = {
    lines += currentLine.replaceAll("""\s+$""", "")
    currentLine = " " * indentLevel
  }

  private[this] def appendBreakable0(str:String):Unit = {
    val words = str.split("""\s+""").filter(_.nonEmpty)
    words.foreach { word =>
      appendUnbreakable0(word, needSpacing = true)
    }
  }

  private[this] def appendUnbreakable0(str:String, needSpacing:Boolean):Unit = {
    if(!hasCurrentLineContent) {
      currentLine += str
    } else if(needSpacing && !cancelNextSpacing && needSpacingBeyond(currentLine.last, str)) {
      val spaced = " " + str
      if(needNewLine(width(spaced))) {
        newLine()
        currentLine += str
      } else {
        currentLine += spaced
      }
    } else if(needNewLine(width(str)) && canNextLine(str)) {
      newLine()
      currentLine += str
    } else {
      currentLine += str
    }
    cancelNextSpacing = false
  }

  private[this] def needSpacingBeyond(c:Char, s:String):Boolean = {
    s.nonEmpty && (s(0) match {
      case ' ' | ')' | ']' | ';' | '.' | ',' => false
      case _ => true
    }) && (c match {
      case ' ' | '[' | '(' => false
      case _ => true
    })
  }

  private[this] def needNewLine(w:Int):Boolean =
    indentLevel < width(currentLine) && w + width(currentLine) > optimalWidth

  private[this] def canNextLine(s:String):Boolean =
    "!.,:;)]}>".contains(s(0)).unary_!

  def width(line:String):Int = {
    line.length
  }

  def renderTable(f: Table.Builder => Unit): Unit = {
    val builder = Table.builder()
    f(builder)
    terminateLine()
    builder.render(optimalWidth) {line =>
      appendRaw(line)
      newLine()
    }
  }
}

object Table {
  def builder() = new Builder()
  class Builder {
    case class Column(num:Int, content:String)
    case class Row(cols:Seq[Column]) {
      def size:Int = cols.size
      def apply(i:Int):Column = cols(i)
    }
    object Row {
      def fromPlain(cols:Seq[String]):Row = {
        Row(
          cols.zipWithIndex.map { case (c, i) => Column(i, c) }
        )
      }
      val empty: Row = Row(Seq())
    }
    var calcDisplayWidth:(String => Int) = { _.size }
    private var header:Option[Row] = None
    private var rows:Seq[Row] = Seq()
    private var widths: Option[Seq[Int]] = None
    def columnSize:Int = header.map(_.size) orElse rows.headOption.map(_.size) getOrElse 0
    def rowSize:Int = rows.size

    def setHeader(header:Seq[String]):this.type = {
      this.header = Some(Row.fromPlain(header))
      this
    }

    def setWidths(ws: Seq[Int]): Unit = {
      this.widths = Some(ws)
    }

    def addRow(cols:Seq[String]):this.type = {
      this.rows ++= Seq(Row.fromPlain(cols))
      this
    }

    def calcWidths(maxWidth:Int):Seq[Int] = {
      val raw = (header orElse rows.headOption getOrElse Row.empty).cols.map { case Column(i, name) =>
          Math.max(calcDisplayWidth(name), rows.map{ r => calcDisplayWidth(r(i).content) }.++(Seq(0)).max)
      }
      optimizeWidth(raw, maxWidth - (2 * columnSize)/*padding*/ - (columnSize - 1)/*sep*/ - 4/*start/end*/)
    }

    def optimizeWidth(ws:Seq[Int], max:Int) = {
      if(ws.sum <= max) ws
      else {
        val base = max / ws.size
        val ws2 = ws.map {w => (w, Math.min(w, base))}
        val rest = max - ws2.map{case (p, r) => r}.sum
        val div = Math.max(ws2.map{case (p,r) => p - r}.sum, 1)
        ws2.map { case (prefer, real) => real + rest * (prefer - real) / div}
      }
    }

    def quadrilateralize[A](self:Seq[Seq[A]], default:A):Seq[Seq[A]] = {
      val w = self.toIterator.map(_.size).max
      self.map {row =>
        for(c <- 0 until w) yield {
          if(c < row.size) row(c) else default
        }
      }
    }

    def pad(self:String, widthFunc:(String=>Int), maxWidth:Int, padding:Char, omit:String):String = {
      val w = widthFunc(self)
      if(w == maxWidth) self
      else if(w < maxWidth) self + padding.toString * (maxWidth - w)
      else shorten(self, widthFunc, maxWidth - widthFunc(omit)) + omit
    }

    def shorten(self:String, widthFunc:(String=>Int), width:Int):String = {
      shorten0(self, widthFunc, 0, width)
    }

    def shorten0(self:String, widthFunc:(String=>Int), index:Int, widthLeft:Int):String = {
      if(index >= self.size) return self

      val w = widthFunc(self.charAt(index).toString)
      if(w > widthLeft) self.substring(0, index) + (" " * widthLeft)
      else shorten0(self, widthFunc, index + 1, widthLeft - w)
    }

    def render(maxWidth:Int)(println:String=>Unit):Unit = {
      if(header.isEmpty && rows.isEmpty)
        return
      var finalWidths = widths getOrElse calcWidths(maxWidth)
      def rowsep(hbar: String) = println("+" + finalWidths.map{w => hbar * (w + 2)}.mkString("+") + "+")
      def outRow(row:Row) = {
        val subRows:Seq[Seq[String]] = quadrilateralize(row.cols.map(_.content.split("\n").toSeq), "").transpose
        val maxHeight = subRows.map(_.size).max
        subRows.zipWithIndex.foreach {case (row, i) =>
          val sep = if(i == 0) "|" else ":"
          println(s"$sep " + row.zipWithIndex.map{case (r, i) => pad(r, calcDisplayWidth(_), finalWidths(i), ' ', "...")}.mkString(s" $sep ") + " |")
        }
      }
      rowsep("-")
      header foreach {h =>
        outRow(h)
        rowsep("=")
      }
      rows.foreach(outRow(_))
      rowsep("-")
    }
  }
}
