package repository

import java.lang.reflect.Field
import java.util.UUID

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 7/4/16.
  **/
class User(implicit coreContext: CoreContext) extends InjectAble {

  val userId = UUID.randomUUID()
  var descr = ""
  recStatus = 1

  override val callContext: CoreContext = coreContext
  override val tableName: String = "users"
  override var fields: Seq[Field] = classOf[User].getDeclaredFields
}
