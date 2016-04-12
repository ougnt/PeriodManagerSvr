package repository

import java.util.UUID

import context.CoreContext
import play.api.libs.json.JsValue

/**
  * * # Created by wacharint on 3/1/16.
  **/
abstract class JsonSerializer {

  def jsonToUsageStatistics(usageStatJson: JsValue)(implicit context: CoreContext): Option[UsageStatistics]
}

class JsonSerializerImpl extends JsonSerializer {
  override def jsonToUsageStatistics(usageStatJson: JsValue)(implicit context: CoreContext): Option[UsageStatistics] = {

    var retStatistic: UsageStatistics = null
    try {
        retStatistic = new UsageStatistics() {
        deviceId = UUID.fromString(usageStatJson \ "deviceId" toString() replace("\"",""))
        applicationVersion = usageStatJson \ "applicationVersion" toString()
        usageCounter = (usageStatJson \ "usageCounter" toString()).toInt
        periodButtonUsageCounter = (usageStatJson \ "periodButtonUsageCounter" toString()).toInt
        nonPeriodButtonUsageCounter = (usageStatJson \ "nonPeriodButtonUsageCounter" toString()).toInt
        comment_button_usage_counter = (usageStatJson \ "comment_button_usage_counter" toString()).toInt
        comment_text_usage_counter = (usageStatJson \ "comment_text_usage_counter" toString()).toInt
        menu_button_usage_counter = (usageStatJson \ "menu_button_usage_counter" toString()).toInt
        review_now = (usageStatJson \ "review_now" toString()).toInt
        review_later = (usageStatJson \ "review_later" toString()).toInt
        review_non = (usageStatJson \ "review_non" toString()).toInt
        fetch_next_usage_counter = (usageStatJson \ "fetch_next_usage_counter" toString()).toInt
        fetch_previous_usage_counter = (usageStatJson \ "fetch_previous_usage_counter" toString()).toInt
        menu_setting_click_counter = (usageStatJson \ "menu_setting_click_counter" toString()).toInt
        menu_summary_click_counter = (usageStatJson \ "menu_summary_click_counter" toString()).toInt
        menu_month_view_click_counter = (usageStatJson \ "menu_month_view_click_counter" toString()).toInt
        menu_help_click_counter = (usageStatJson \ "menu_help_click_counter" toString()).toInt
        menu_review_click_counter = (usageStatJson \ "menu_review_click_counter" toString()).toInt

        // These fields available in the version 26
        if(Integer.parseInt(applicationVersion) >= 26) {

          setting_notify_period_usage_counter = (usageStatJson \ "setting_notify_period_usage_counter" toString()).toInt
          setting_notify_ovulation_usage_counter = (usageStatJson \ "setting_notify_ovulation_usage_counter" toString()).toInt
          setting_notify_period_days = (usageStatJson \ "setting_notify_period_days" toString()).toInt
          setting_notify_ovulation_days = (usageStatJson \ "setting_notify_ovulation_days" toString()).toInt
          setting_notify_notification_click_counter = (usageStatJson \ "setting_notify_notification_click_counter" toString()).toInt
        }

        // This field available in the version 29
        if(Integer.parseInt(applicationVersion) >= 29) {
          setting_displayed_language = (usageStatJson \ "setting_displayed_language" toString()) replace("\"", "")
          setting_language_change_usage_counter = (usageStatJson \ "setting_language_change_usage_counter" toString()).toInt
        }
      }
    } catch {
      case e: Exception => return None
    }

    Some(retStatistic)
  }
}