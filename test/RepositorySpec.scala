import java.util.UUID

import context.CoreContext
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import repository.{Device, UsageStatistics}

/**
  * * # Created by wacharint on 2/19/16.
  **/
class RepositorySpec extends Specification {

  """UsageStatistics""" should {

    """be able to inject""" in {

      // setup
      implicit var context = new CoreContext
      context.connect()
      val device = new Device() {
        recStatus = 1
      }

      val usageStat = new UsageStatistics() {
        deviceId = device.deviceId
        applicationVersion = "1"

        recStatus = 1
      }

      // excute
      device.insert()
      usageStat.insert()

      // verify
      val res = new UsageStatistics().get("device_id", usageStat.deviceId.toString).head.asInstanceOf[UsageStatistics]

      res must beSameUsageStatistics(usageStat)
    }
  }

  """device""" should {

    """be able to be injected""" in {

      // Setup
      implicit var context = new CoreContext
      context.connect()
      val initial = new Device(){
        deviceId = UUID.randomUUID()
      }

      // Execute
      initial.insert()

      // Verify
      val ret = new Device().get("device_id", initial.deviceId.toString).head.asInstanceOf[Device]
      ret must beSameDevice(initial)
    }
  }

  def beSameDevice(expect: Device): Matcher[Device] = (actual: Device) => (
    expect.deviceId == actual.deviceId,
    "Same device",
    "Both device are difference"
    )

  def beSameUsageStatistics(expect : UsageStatistics) : Matcher[UsageStatistics] = (actual: UsageStatistics) => (
    expect.applicationVersion == actual.applicationVersion &&
    expect.deviceId == actual.deviceId &&
    expect.usageCounter == actual.usageCounter &&
    expect.periodButtonUsageCounter == actual.periodButtonUsageCounter &&
    expect.nonPeriodButtonUsageCounter == actual.nonPeriodButtonUsageCounter &&
    expect.comment_button_usage_counter == actual.comment_button_usage_counter &&
    expect.comment_text_usage_counter == actual.comment_text_usage_counter &&
    expect.menu_button_usage_counter == actual.menu_button_usage_counter &&
    expect.review_now == actual.review_now &&
    expect.review_later == actual.review_later &&
    expect.review_non == actual.review_non &&
    expect.fetch_next_usage_counter == actual.fetch_next_usage_counter &&
    expect.fetch_previous_usage_counter == actual.fetch_previous_usage_counter &&
    expect.menu_setting_click_counter == actual.menu_setting_click_counter &&
    expect.menu_summary_click_counter == actual.menu_summary_click_counter &&
    expect.menu_month_view_click_counter == actual.menu_month_view_click_counter &&
    expect.menu_help_click_counter == actual.menu_help_click_counter,
    "Same UsageStatistics",
    "Difference UsageStatistics"
    )
}
