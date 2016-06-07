package repository

import java.lang.reflect.Field
import java.util.UUID

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 5/25/16.
  **/
class UsageDuration(implicit coreContext: CoreContext) extends InjectAble{

  var duration_id = 0l
  var device_id : UUID = UUID.randomUUID()
  var data_date = ""
  var data_hour = 0
  var applicationVersion = "0"
  var duration = 0

  override val callContext: CoreContext = coreContext
  override val tableName: String = "usage_duration"
  override var fields: Seq[Field] = classOf[UsageDuration].getDeclaredFields
}
