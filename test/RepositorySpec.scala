import java.sql.SQLException
import java.util.UUID

import context.CoreContext
import org.joda.time.DateTime
import org.specs2.matcher.Matcher
import play.api.libs.json.Json
import repository._

/**
  * * # Created by wacharint on 2/19/16.
  **/
class RepositorySpec extends BasedSpec {

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
      val res = new UsageStatistics().get(Seq(("device_id", usageStat.deviceId.toString))).head.asInstanceOf[UsageStatistics]

      res must beSameUsageStatistics(usageStat)
    }

    """be able to be updated when the key is existing with function insertOrUpdate""" in {

      // Setup
      implicit var context = new CoreContext
      context.connect()
      val device = new Device() {
        recStatus = 1
        language = "th"
      }

      val usageStat = new UsageStatistics() {
        deviceId = device.deviceId
        applicationVersion = "1"
        setting_displayed_language = "th"
        recStatus = 1
      }
      device.insert()
      usageStat.insert()

      usageStat.comment_button_usage_counter = 100

      // Execute
      usageStat.insertOrUpdate(Seq(("device_id", usageStat.deviceId.toString),("application_version", usageStat.applicationVersion)))

      // Verify
      val ret = new UsageStatistics().get(Seq(("device_id", usageStat.deviceId.toString))).head.asInstanceOf[UsageStatistics]

      ret must beSameUsageStatistics(usageStat)
    }

    """be able to be inserted when the key is not existing with function insertOrUpdate""" in {

      // Setup
      implicit var context = new CoreContext
      context.connect()
      val device = new Device() {
        recStatus = 1
        language = "th"
      }

      val usageStat = new UsageStatistics() {
        deviceId = device.deviceId
        applicationVersion = "1"
        setting_displayed_language = "th"
        setting_language_change_usage_counter = 1
        recStatus = 1
      }
      device.insert()
      usageStat.applicationVersion = "2"
      usageStat.comment_button_usage_counter = 22

      // Execute
      usageStat.insertOrUpdate(Seq(("device_id", usageStat.deviceId.toString),("application_version", usageStat.applicationVersion)))

      // Verify
      val ret = new UsageStatistics().get(Seq(("device_id", usageStat.deviceId.toString),("application_version", usageStat.applicationVersion))).head.asInstanceOf[UsageStatistics]

      ret must beSameUsageStatistics(usageStat)
    }

    """reconnect when the connection is disconnected with function insertOrUpdate""" in {

      // Setup
      implicit var context = new CoreContext
      val device = new Device() {
        recStatus = 1
      }

      val usageStat = new UsageStatistics() {
        deviceId = device.deviceId
        applicationVersion = "1"

        recStatus = 1
      }
      device.insert()
      usageStat.insert()

      usageStat.menu_summary_click_counter = 100

      // Execute
      usageStat.insertOrUpdate(Seq(("device_id", usageStat.deviceId.toString),("application_version", usageStat.applicationVersion)))

      // Verify
      val ret = new UsageStatistics().get(Seq(("device_id", usageStat.deviceId.toString))).head.asInstanceOf[UsageStatistics]

      ret must beSameUsageStatistics(usageStat)
    }

    """throw SQLException when the username is incorrect with function insertOrUpdate""" in {

      // Setup
      implicit var context = new CoreContext
      context.url = "jdbc:mysql://localhost:3306/period_manager?user=invalid&password=invalid"
      val device = new Device() {
        recStatus = 1
      }

      val usageStat = new UsageStatistics() {
        deviceId = device.deviceId
        applicationVersion = "1"

        recStatus = 1
      }
      device.insert()  must throwA[SQLException]
      usageStat.insert() must throwA[SQLException]

      usageStat.applicationVersion = "2"

      // Execute // Verify
      usageStat.insertOrUpdate(Seq(("device_id", usageStat.deviceId.toString),("application_version", usageStat.applicationVersion))) must throwA[SQLException]("""Access denied for user""")
    }

    """throw SQLException when the connection is down with function insertOrUpdate""" in {

      // Setup
      implicit var context = new CoreContext
      context.url = "jdbc:mysql://localhost:9999/period_manager?user=root&password="
      val device = new Device() {
        recStatus = 1
      }

      val usageStat = new UsageStatistics() {
        deviceId = device.deviceId
        applicationVersion = "1"

        recStatus = 1
      }
      device.insert()  must throwA[SQLException]
      usageStat.insert() must throwA[SQLException]

      usageStat.applicationVersion = "2"

      // Execute // Verify
      usageStat.insertOrUpdate(Seq(("device_id", usageStat.deviceId.toString),("application_version", usageStat.applicationVersion))) must throwA[SQLException]("Communications link failure")
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
      val ret = new Device().get(Seq(("device_id", initial.deviceId.toString))).head.asInstanceOf[Device]
      ret must beSameDevice(initial)
    }
  }

  """DailyUsage""" should {

    """be able to be injected and get""" in {

      // Setup
      val initialDevice = new Device
      initialDevice.insert()

      val usage = new DailyUsage{
        deviceId = initialDevice.deviceId
        dataDate = DateTime.now.toString("YYYY-MM-DD")
        dataHour = 12
        usageCounter = 2
      }

      //Execute
      usage.insert()

      // Verify
      val retUsage: Seq[DailyUsage] = new DailyUsage().get(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "12"),
        ("data_date", DateTime.now.toString("YYYY-MM-DD")))
      ).asInstanceOf[Seq[DailyUsage]]

      retUsage.size mustEqual 1
      retUsage.head must beSameDailyUsage(usage)
    }

    """be able to be updated and get""" in {

      // Setup
      val initialDevice = new Device
      initialDevice.insert()

      val usage = new DailyUsage{
        deviceId = initialDevice.deviceId
        dataDate = DateTime.now.toString("YYYY-MM-dd")
        dataHour = 12
        usageCounter = 2
      }

      usage.insert

      usage.usageCounter = 10

      //Execute
      usage.insertOrUpdate(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "12"),
        ("data_date", DateTime.now.toString("YYYY-MM-dd")))
      )

      // Verify
      val retUsage: Seq[DailyUsage] = new DailyUsage().get(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "12"),
        ("data_date", DateTime.now.toString("YYYY-MM-dd")))
      ).asInstanceOf[Seq[DailyUsage]]

      retUsage.size mustEqual 1
      retUsage.head must beSameDailyUsage(usage)
    }

    """create a new entry when the data is update in a difference time but the same day""" in {

      // Setup
      val initialDevice = new Device
      initialDevice.insert()

      val usage = new DailyUsage{
        deviceId = initialDevice.deviceId
        dataDate = DateTime.now.toString("YYYY-MM-dd")
        dataHour = 11
        usageCounter = 2
      }

      usage.insert

      usage.usageCounter = 10
      usage.dataHour = 12

      //Execute
      usage.insertOrUpdate(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "12"),
        ("data_date", DateTime.now.toString("YYYY-MM-dd")))
      )

      // Verify
      var retUsage: Seq[DailyUsage] = new DailyUsage().get(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "12"),
        ("data_date", DateTime.now.toString("YYYY-MM-dd")))
      ).asInstanceOf[Seq[DailyUsage]]

      retUsage.size mustEqual 1
      retUsage.head must beSameDailyUsage(usage)

      retUsage = new DailyUsage().get(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "11"),
        ("data_date", DateTime.now.toString("YYYY-MM-dd")))
      ).asInstanceOf[Seq[DailyUsage]]

      retUsage.size mustEqual 1
    }

    """be able to be updated and get when there is another record of the device in other day""" in {

      // Setup
      val initialDevice = new Device
      initialDevice.insert()

      val usage = new DailyUsage{
        deviceId = initialDevice.deviceId
        dataDate = DateTime.now.minusDays(1).toString("YYYY-MM-dd")
        dataHour = 12
        usageCounter = 2
      }

      usage.insert

      usage.usageCounter = 10
      usage.dataDate = DateTime.now.toString("YYYY-MM-dd")

      //Execute
      usage.insertOrUpdate(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "12"),
        ("data_date", DateTime.now.toString("YYYY-MM-dd")))
      )

      // Verify
      val retUsage: Seq[DailyUsage] = new DailyUsage().get(Seq(
        ("device_id", initialDevice.deviceId.toString),
        ("data_hour", "12"),
        ("data_date", DateTime.now.toString("YYYY-MM-dd")))
      ).asInstanceOf[Seq[DailyUsage]]

      retUsage.size mustEqual 1
      retUsage.head must beSameDailyUsage(usage)
    }
  }

  """JsonSerializer""" should {

    """be able to deserialize the version 25 usageStatistics json""" in {

      // Setup
      val json = Json.parse("""{
                                 "deviceId":"65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f",
                                 "applicationVersion":25,
                                 "usageCounter":1,
                                 "periodButtonUsageCounter":2,
                                 "nonPeriodButtonUsageCounter":3,
                                 "comment_button_usage_counter":4,
                                 "comment_text_usage_counter":5,
                                 "menu_button_usage_counter":6,
                                 "review_now":7,
                                 "review_later":8,
                                 "review_non":9,
                                 "fetch_next_usage_counter":10,
                                 "fetch_previous_usage_counter":11,
                                 "menu_setting_click_counter":12,
                                 "menu_summary_click_counter":13,
                                 "menu_month_view_click_counter":14,
                                 "menu_help_click_counter":15,
                                "menu_review_click_counter": 16
                              }""")
      val serializer = new JsonSerializerImpl

      val expectedUsageStat = new UsageStatistics() {
        deviceId = UUID.fromString("65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f")
        applicationVersion = "25"
        usageCounter = 1
        periodButtonUsageCounter = 2
        nonPeriodButtonUsageCounter = 3
        comment_button_usage_counter = 4
        comment_text_usage_counter = 5
        menu_button_usage_counter = 6
        review_now = 7
        review_later = 8
        review_non = 9
        fetch_next_usage_counter = 10
        fetch_previous_usage_counter = 11
        menu_setting_click_counter = 12
        menu_summary_click_counter = 13
        menu_month_view_click_counter = 14
        menu_help_click_counter = 15
        menu_review_click_counter = 16
        setting_notify_period_usage_counter = 0
        setting_notify_ovulation_usage_counter = 0
        setting_notify_period_days = 0
        setting_notify_ovulation_days = 0
        setting_notify_notification_click_counter = 0
      }

      // Execute
      val res = serializer.jsonToUsageStatistics(json).get

      // Verify
      res must beSameUsageStatistics(expectedUsageStat)
    }

    """be able to deserialize the version 26 usageStatistics json""" in {

      // Setup
      val json = Json.parse("""{
                                 "deviceId":"65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f",
                                 "applicationVersion":26,
                                 "usageCounter":1,
                                 "periodButtonUsageCounter":2,
                                 "nonPeriodButtonUsageCounter":3,
                                 "comment_button_usage_counter":4,
                                 "comment_text_usage_counter":5,
                                 "menu_button_usage_counter":6,
                                 "review_now":7,
                                 "review_later":8,
                                 "review_non":9,
                                 "fetch_next_usage_counter":10,
                                 "fetch_previous_usage_counter":11,
                                 "menu_setting_click_counter":12,
                                 "menu_summary_click_counter":13,
                                 "menu_month_view_click_counter":14,
                                 "menu_help_click_counter":15,
                                "menu_review_click_counter": 16,
                                "setting_notify_period_usage_counter": 17,
                                "setting_notify_ovulation_usage_counter": 18,
                                "setting_notify_period_days": 19,
                                "setting_notify_ovulation_days": 20,
                                "setting_notify_notification_click_counter": 21
                              }""")
      val serializer = new JsonSerializerImpl

      val expectedUsageStat = new UsageStatistics() {
        deviceId = UUID.fromString("65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f")
        applicationVersion = "26"
        usageCounter = 1
        periodButtonUsageCounter = 2
        nonPeriodButtonUsageCounter = 3
        comment_button_usage_counter = 4
        comment_text_usage_counter = 5
        menu_button_usage_counter = 6
        review_now = 7
        review_later = 8
        review_non = 9
        fetch_next_usage_counter = 10
        fetch_previous_usage_counter = 11
        menu_setting_click_counter = 12
        menu_summary_click_counter = 13
        menu_month_view_click_counter = 14
        menu_help_click_counter = 15
        menu_review_click_counter = 16
        setting_notify_period_usage_counter = 17
        setting_notify_ovulation_usage_counter = 18
        setting_notify_period_days = 19
        setting_notify_ovulation_days = 20
        setting_notify_notification_click_counter = 21
      }

      // Execute
      val res = serializer.jsonToUsageStatistics(json).get

      // Verify
      res must beSameUsageStatistics(expectedUsageStat)
    }

    """be able to deserialize the version 29 usageStatistics json""" in {

      // Setup
      val json = Json.parse("""{
                                 "deviceId":"65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f",
                                 "applicationVersion":29,
                                 "usageCounter":1,
                                 "periodButtonUsageCounter":2,
                                 "nonPeriodButtonUsageCounter":3,
                                 "comment_button_usage_counter":4,
                                 "comment_text_usage_counter":5,
                                 "menu_button_usage_counter":6,
                                 "review_now":7,
                                 "review_later":8,
                                 "review_non":9,
                                 "fetch_next_usage_counter":10,
                                 "fetch_previous_usage_counter":11,
                                 "menu_setting_click_counter":12,
                                 "menu_summary_click_counter":13,
                                 "menu_month_view_click_counter":14,
                                 "menu_help_click_counter":15,
                                "menu_review_click_counter": 16,
                                "setting_notify_period_usage_counter": 17,
                                "setting_notify_ovulation_usage_counter": 18,
                                "setting_notify_period_days": 19,
                                "setting_notify_ovulation_days": 20,
                                "setting_notify_notification_click_counter": 21,
                                "setting_language_change_usage_counter" : 1,
                                "setting_displayed_language" : "th"
                              }""")
      val serializer = new JsonSerializerImpl

      val expectedUsageStat = new UsageStatistics() {
        deviceId = UUID.fromString("65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f")
        applicationVersion = "29"
        usageCounter = 1
        periodButtonUsageCounter = 2
        nonPeriodButtonUsageCounter = 3
        comment_button_usage_counter = 4
        comment_text_usage_counter = 5
        menu_button_usage_counter = 6
        review_now = 7
        review_later = 8
        review_non = 9
        fetch_next_usage_counter = 10
        fetch_previous_usage_counter = 11
        menu_setting_click_counter = 12
        menu_summary_click_counter = 13
        menu_month_view_click_counter = 14
        menu_help_click_counter = 15
        menu_review_click_counter = 16
        setting_notify_period_usage_counter = 17
        setting_notify_ovulation_usage_counter = 18
        setting_notify_period_days = 19
        setting_notify_ovulation_days = 20
        setting_notify_notification_click_counter = 21
        setting_displayed_language = "th"
        setting_language_change_usage_counter = 1
      }

      // Execute
      val res = serializer.jsonToUsageStatistics(json).get

      // Verify
      res must beSameUsageStatistics(expectedUsageStat)
    }

    """be able to deserialize the version 25 usageStatistics json with version 26 json""" in {

      // Setup
      val json = Json.parse("""{
                                 "deviceId":"65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f",
                                 "applicationVersion":25,
                                 "usageCounter":1,
                                 "periodButtonUsageCounter":2,
                                 "nonPeriodButtonUsageCounter":3,
                                 "comment_button_usage_counter":4,
                                 "comment_text_usage_counter":5,
                                 "menu_button_usage_counter":6,
                                 "review_now":7,
                                 "review_later":8,
                                 "review_non":9,
                                 "fetch_next_usage_counter":10,
                                 "fetch_previous_usage_counter":11,
                                 "menu_setting_click_counter":12,
                                 "menu_summary_click_counter":13,
                                 "menu_month_view_click_counter":14,
                                 "menu_help_click_counter":15,
                                "menu_review_click_counter": 16,
                                "setting_notify_period_usage_counter": 17,
                                "setting_notify_ovulation_usage_counter": 18,
                                "setting_notify_period_days": 19,
                                "setting_notify_ovulation_days": 20,
                                "setting_notify_notification_click_counter": 21
                              }""")
      val serializer = new JsonSerializerImpl

      val expectedUsageStat = new UsageStatistics() {
        deviceId = UUID.fromString("65f622f1-ad56-4ccf-a2f2-d58fe0c2ed9f")
        applicationVersion = "25"
        usageCounter = 1
        periodButtonUsageCounter = 2
        nonPeriodButtonUsageCounter = 3
        comment_button_usage_counter = 4
        comment_text_usage_counter = 5
        menu_button_usage_counter = 6
        review_now = 7
        review_later = 8
        review_non = 9
        fetch_next_usage_counter = 10
        fetch_previous_usage_counter = 11
        menu_setting_click_counter = 12
        menu_summary_click_counter = 13
        menu_month_view_click_counter = 14
        menu_help_click_counter = 15
        menu_review_click_counter = 16
        setting_notify_period_usage_counter = 0
        setting_notify_ovulation_usage_counter = 0
        setting_notify_period_days = 0
        setting_notify_ovulation_days = 0
        setting_notify_notification_click_counter = 0
      }

      // Execute
      val res = serializer.jsonToUsageStatistics(json).get

      // Verify
      res must beSameUsageStatistics(expectedUsageStat)
    }
  }

  def beSameDevice(expect: Device): Matcher[Device] = (actual: Device) => (
    expect.deviceId == actual.deviceId &&
    expect.language.equals(actual.language),
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
    expect.menu_help_click_counter == actual.menu_help_click_counter &&
    expect.setting_notify_notification_click_counter == actual.setting_notify_notification_click_counter &&
    expect.setting_notify_ovulation_days == actual.setting_notify_ovulation_days &&
    expect.setting_notify_ovulation_usage_counter == actual.setting_notify_ovulation_usage_counter &&
    expect.setting_notify_period_days == actual.setting_notify_period_days &&
    expect.setting_notify_period_usage_counter == actual.setting_notify_period_usage_counter &&
    expect.setting_language_change_usage_counter == actual.setting_language_change_usage_counter &&
    expect.setting_displayed_language.equals(actual.setting_displayed_language),
    "Same UsageStatistics",
    "Difference UsageStatistics"
    )

  def beSameDailyUsage(expect: DailyUsage): Matcher[DailyUsage] = (actual: DailyUsage) => (
    expect.dataDate.toString().equals(actual.dataDate.toString()) &&
    expect.dataHour == actual.dataHour &&
    expect.deviceId.toString.equals(actual.deviceId.toString) &&
    expect.usageCounter == actual.usageCounter,
    "Same daily usage",
    "The entered DailyUsages are not the same"
    )

  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}
}
