package de.sciss.connect
package view

import de.sciss.lucre.stm
import de.sciss.synth.proc.Attribute
import de.sciss.connect.view.impl.{UGenSourceRenderer, BooleanRenderer, ToStringRenderer}
import prefuse.data.Node
import de.sciss.synth.UGenSpec
import de.sciss.lucre.synth.Sys

sealed trait VisualBoxLike /* [S <: Sys[S]] */ {
  //  var name: String = ""
  protected def defaultState: ElementState = ElementState.Ok

  var state: ElementState = defaultState

  def renderer: ElementRenderer

  def ports: VisualPorts

  def value: Any

  private var _node = Option.empty[Node]

  def init(node: Node): Unit = {
    requireEDT()
    require(_node.isEmpty, "Already initialized")
    _node = Some(node)
  }

  def node: Option[Node] = {
    requireEDT()
    _node
  }
}

sealed trait VisualBox[S <: Sys[S]] extends VisualBoxLike {
  def source: stm.Source[S#Tx, Attribute[S]]
}

//object VisualProduct {
//  def unapply[S <: Sys[S]](p: VisualProduct[S]): Option[stm.Source[S#Tx, Product[S]]] = p.content
//}
//class VisualProduct[S <: Sys[S]](val source: stm.Source[S#Tx, Product[S]], var value: Any)
//  extends VisualElementT[S] {
//
//  def ports = ???
//
//  def renderer: ElementRenderer = ???
//}

class VisualUGenSource[S <: Sys[S]](val source: stm.Source[S#Tx, UGenSource[S]], spec: UGenSpec)
  extends VisualBox[S] {

  def value = spec

  val ports = VisualPorts(numIns = spec.args.size, numOuts = spec.outputs.size)

  def renderer: ElementRenderer = UGenSourceRenderer
}

class VisualIncompleteElement[S <: Sys[S]](val source: stm.Source[S#Tx, IncompleteElement[S]], var value: String)
  extends VisualBox[S] {

  override def defaultState = ElementState.Edit

  val ports = VisualPorts(0, 0)

  def renderer: ElementRenderer = ToStringRenderer
}

class VisualInt[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Int[S]], var value: Int)
  extends VisualBox[S] {

  val ports = VisualPorts(numIns = 0, numOuts = 1)

  def renderer: ElementRenderer = ToStringRenderer
}

class VisualBoolean[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Boolean[S]], var value: Boolean)
  extends VisualBox[S] {

  val ports = VisualPorts(numIns = 0, numOuts = 1)

  def renderer: ElementRenderer = BooleanRenderer
}

// class VisualEdge[S <: Sys[S]](val source: stm.Source[S#Tx, Connection[S]])
case class VisualEdge(outlet: Int, inlet: Int)