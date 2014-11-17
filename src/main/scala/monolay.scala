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
    if(lines.isEmpty && !hasCurrentLineContent)
      ""
    else if(hasCurrentLineContent)
      lines.mkString("\n") + currentLine + "\n"
    else
      lines.mkString("\n")

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
}
