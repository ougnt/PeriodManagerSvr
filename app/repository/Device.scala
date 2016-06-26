package repository

import java.lang.reflect.Field
import java.util.UUID

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 2/25/16.
  **/
class Device(implicit context: CoreContext) extends InjectAble {

  var deviceId: UUID = UUID.randomUUID()
  var language: String = ""
  var applicationVersion = "0"

  override val callContext: CoreContext = context
  override val tableName: String = "device_info"
  override var fields: Seq[Field] = classOf[Device].getDeclaredFields
}
