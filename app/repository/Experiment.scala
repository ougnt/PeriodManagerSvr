package repository

import java.lang.reflect.Field

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 4/19/16.
  **/
class Experiment(implicit context: CoreContext) extends InjectAble {

  var experimentId: Int = 0
  var description = ""
  recStatus = 1

  override val callContext: CoreContext = context
  override val tableName: String = "experiment"
  override var fields: Seq[Field] = classOf[Experiment].getDeclaredFields
}