package repository

import java.lang.reflect.Field
import java.util.UUID

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 8/27/15.
  **/
class UsageStatistics(implicit context: CoreContext) extends InjectAble {

  var deviceId : UUID = UUID.randomUUID()
  var applicationVersion = ""
  var usageCounter = 0
  var periodButtonUsageCounter = 0
  var nonPeriodButtonUsageCounter = 0
  var comment_button_usage_counter = 0
  var comment_text_usage_counter = 0
  var menu_button_usage_counter = 0
  var review_now = 0
  var review_later = 0
  var review_non = 0
  var fetch_next_usage_counter = 0
  var fetch_previous_usage_counter = 0
  var menu_setting_click_counter = 0
  var menu_summary_click_counter = 0
  var menu_month_view_click_counter = 0
  var menu_help_click_counter = 0
  var menu_review_click_counter = 0

  override val callContext: CoreContext = context
  override val tableName: String = "usage_stat"
  override var fields: Seq[Field] = classOf[UsageStatistics].getDeclaredFields
}
