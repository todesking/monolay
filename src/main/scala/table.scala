package com.todesking.monolay.table

import com.todesking.monolay.DisplayWidthRule

class Builder {
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

  def render(maxWidth: Int, displayWidth: DisplayWidthRule)(println: String=>Unit): Unit = {
    if(header.isEmpty && rows.isEmpty)
      return

    var finalWidths = widths getOrElse ColumnWidthRule.optimized(maxWidth, displayWidth).widths(header, rows)

    val println_ = println
    val renderer = new Renderer {
      override def println(line: String): Unit = println_(line)
      override val widths = finalWidths
      override val displayWidthRule = displayWidth
    }

    renderer.render(header, rows, displayWidth)
  }
}

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
case class Column(num:Int, content:String)

trait ColumnWidthRule {
  def widths(header: Option[Row], rows: Seq[Row]): Seq[Int]
}

object ColumnWidthRule {
  def optimized(maxWidth: Int, displayWidthRule: DisplayWidthRule) = new ColumnWidthRule {
    override def widths(header: Option[Row], rows: Seq[Row]): Seq[Int] = {
      val raw = (header orElse rows.headOption getOrElse Row.empty).cols.map { case Column(i, name) =>
          Math.max(displayWidthRule.widthOf(name), rows.map{ r => displayWidthRule.widthOf(r(i).content) }.++(Seq(0)).max)
      }
      val columnSize = raw.size
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
  }
}

trait Renderer {
  def println(line: String): Unit
  val widths: Seq[Int]
  val displayWidthRule: DisplayWidthRule

  def render(header: Option[Row], rows: Seq[Row], displayWidth: DisplayWidthRule): Unit = {
    renderBorder('-')
    header foreach {h =>
      renderRow(h)
      renderBorder('=')
    }
    rows.foreach(renderRow(_))
    renderBorder('-')
  }

  def renderBorder(ch: Char) =
    println("+" + widths.map{w => ch.toString * (w + 2)}.mkString("+") + "+")

  def renderRow(row:Row) = {
    val subRows:Seq[Seq[String]] = quadrilateralize(row.cols.map(_.content.split("\n").toSeq), "").transpose
    val maxHeight = subRows.map(_.size).max
    subRows.zipWithIndex.foreach {case (row, i) =>
      val sep = if(i == 0) "|" else ":"
      println(s"$sep " + row.zipWithIndex.map{case (r, i) => pad(r, displayWidthRule.widthOf(_), widths(i), ' ', "...")}.mkString(s" $sep ") + " |")
    }
  }

  def pad(self:String, widthFunc:(String=>Int), maxWidth:Int, padding:Char, omit:String):String = {
    val w = widthFunc(self)
    if(w == maxWidth) self
    else if(w < maxWidth) self + padding.toString * (maxWidth - w)
    else shorten(self, widthFunc, maxWidth - widthFunc(omit)) + omit
  }

  def quadrilateralize[A](self:Seq[Seq[A]], default:A):Seq[Seq[A]] = {
    val w = self.toIterator.map(_.size).max
    self.map {row =>
      for(c <- 0 until w) yield {
        if(c < row.size) row(c) else default
      }
    }
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


}
