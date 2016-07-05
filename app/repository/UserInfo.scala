package repository

import java.lang.reflect.Field
import java.util.UUID

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 6/27/16.
  **/
class UserInfo(implicit coreContext: CoreContext) extends InjectAble {

  var userInfoId = 0l
  var userId = UUID.randomUUID()
  var userToken = UUID.randomUUID()
  var userEmail = ""
  var password = ""
  recStatus = 1

  override val callContext: CoreContext = coreContext
  override val tableName: String = "user_info"
  override var fields: Seq[Field] = classOf[UserInfo].getDeclaredFields
}
