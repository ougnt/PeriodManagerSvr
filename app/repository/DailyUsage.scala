package repository

import java.lang.reflect.Field
import java.util.UUID

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 3/23/16.
  **/
class DailyUsage(implicit context: CoreContext) extends InjectAble {

  var deviceId: UUID = null
  var dataDate: String = null
  var dataHour: Int = 0
  var usageCounter: Int = 0

  override val callContext: CoreContext = context
  override val tableName: String = "daily_usage"
  override var fields: Seq[Field] = classOf[DailyUsage].getDeclaredFields

}
