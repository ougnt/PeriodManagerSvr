package repository

import java.lang.reflect.Field

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 10/9/2016 AD.
  **/
class ErrorLog(implicit context: CoreContext) extends InjectAble {

  var errorId = 0
  var applicationVersion = ""
  var errorMessage = ""
  var stacktrace = ""

  override val callContext: CoreContext = context
  override val tableName: String = "error_log"
  override var fields: Seq[Field] = classOf[ErrorLog].getDeclaredFields
}
