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

  """Experiment""" should {

    """be able to be inserted""" in {

      // Setup
      val testExperiment: Experiment = new Experiment{

        description = "test"
      }

      // Execute
      testExperiment.insert

      // Verify
      new Experiment().get(Seq(("description", "test"))).head.asInstanceOf[Experiment] must beSameExperiment(testExperiment)
    }

    """be able to be updated""" in {

      // Setup
      val testExperiment: Experiment = new Experiment{

        description = "test"
      }
      testExperiment.insert
      testExperiment.description = "test2"

      // Execute
      testExperiment.insertOrUpdate(Seq(
        ("experiment_id",new Experiment().get(Seq(("description", "test"))).head.asInstanceOf[Experiment].experimentId.toString)
      ))

      // Verify
      new Experiment().get(Seq(("description", "test2"))).head.asInstanceOf[Experiment] must beSameExperiment(testExperiment)
    }
  }

  """ExperimentAdsRun""" should {

    """be able to be inserted""" in {

      // Setup
      var testExperiment: Experiment = new Experiment()
      testExperiment.insert()
      testExperiment = testExperiment.get(Seq(("description",""))).head.asInstanceOf[Experiment]

      val testExperimentRun: ExperimentAdsRun = new ExperimentAdsRun {

        experimentId = testExperiment.experimentId
        displayedLanguage = "th"
        aAdsClick = 0
        aAdsShow = 1
        aAdsUrl = "http://a.com/a"
        bAdsClick = 2
        bAdsShow = 3
        bAdsUrl = "http://a.com/b"
        cAdsClick = 4
        cAdsShow = 5
        cAdsUrl = "http://a.com/c"
        dAdsClick = 6
        dAdsShow = 7
        dAdsUrl = "http://a.com/d"
        eAdsClick = 8
        eAdsShow = 9
        eAdsUrl = "http://a.com/e"
        fAdsClick = 10
        fAdsShow = 11
        fAdsUrl = "http://a.com/f"
      }

      // Execute
      testExperimentRun.insert

      // Verify
      val actual = new ExperimentAdsRun().get(Seq(("experiment_id",testExperimentRun.experimentId.toString))).head.asInstanceOf[ExperimentAdsRun]
      actual.experimentRunId = 0
      actual must beSameExperimentAdsRun(testExperimentRun)
    }

    """be able to be updated""" in {

      // Setup
      var testExperiment: Experiment = new Experiment()
      testExperiment.insert()
      testExperiment = testExperiment.get(Seq(("description",""))).head.asInstanceOf[Experiment]

      val testExperimentRun: ExperimentAdsRun = new ExperimentAdsRun {

        experimentId = testExperiment.experimentId
        displayedLanguage = "th"
        aAdsClick = 0
        aAdsShow = 1
        aAdsUrl = "http://a.com/a"
        bAdsClick = 2
        bAdsShow = 3
        bAdsUrl = "http://a.com/b"
        cAdsClick = 4
        cAdsShow = 5
        cAdsUrl = "http://a.com/c"
        dAdsClick = 6
        dAdsShow = 7
        dAdsUrl = "http://a.com/d"
        eAdsClick = 8
        eAdsShow = 9
        eAdsUrl = "http://a.com/e"
        fAdsClick = 10
        fAdsShow = 11
        fAdsUrl = "http://a.com/f"
      }
      testExperimentRun.insert

      // Execute
      val expect = new ExperimentAdsRun().get(Seq(("experiment_id",testExperimentRun.experimentId.toString))).head.asInstanceOf[ExperimentAdsRun]
      expect.aAdsClick = 10
      expect.insertOrUpdate(Seq(("experiment_run_id",expect.experimentRunId.toString)))

      // Verify
      val actual = new ExperimentAdsRun().get(Seq(("experiment_id",testExperimentRun.experimentId.toString))).head.asInstanceOf[ExperimentAdsRun]
      actual must beSameExperimentAdsRun(expect)
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

  """UsageDuration""" should {

    """be able to be inserted""" in {
      // Setup
      val device = new Device {
        deviceId = UUID.randomUUID
        language = "th"
      }
      device.insert()

      val testUsageDuration = new UsageDuration {
        device_id = device.deviceId
        data_date = "2016-05-30"
        data_hour = 22
        duration = 12345678
      }

      // Execute
      val insertUsageId = testUsageDuration.insert()

      // Verify
      val actualUsage = new UsageDuration().get(Seq(("duration_id", insertUsageId toString))).asInstanceOf[Seq[UsageDuration]].head
      actualUsage must beSameUsageDuration(testUsageDuration)
    }

    """be able to duplication insert""" in {
      // Setup
      val device = new Device {
        deviceId = UUID.randomUUID
        language = "th"
      }
      device.insert()

      val testUsageDuration = new UsageDuration {
        device_id = device.deviceId
        data_date = "2016-05-30"
        data_hour = 22
        duration = 12345678
      }

      // Execute
      val insertUsageId1 = testUsageDuration.insert()
      val insertUsageId2 = testUsageDuration.insert()

      // Verify
      val actualUsage1 = new UsageDuration().get(Seq(("duration_id", insertUsageId1 toString))).asInstanceOf[Seq[UsageDuration]].head
      val actualUsage2 = new UsageDuration().get(Seq(("duration_id", insertUsageId2 toString))).asInstanceOf[Seq[UsageDuration]].head
      actualUsage1 must beSameUsageDuration(actualUsage2)
    }

    """throw exception when the device ID doesn't existing""" in {
      // Setup
      val testUsageDuration = new UsageDuration {
        device_id = UUID.randomUUID
        data_date = "2016-05-30"
        data_hour = 22
        duration = 12345678
      }

      // Execute
      testUsageDuration.insert() must throwA[Exception]
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

  def beSameExperiment(expect: Experiment): Matcher[Experiment] = (actual: Experiment) => (
    expect.description.equals(actual.description),
    "Same experiment",
    "experimentId is not match"
  )

  def beSameExperimentAdsRun(expect: ExperimentAdsRun): Matcher[ExperimentAdsRun] = (actual: ExperimentAdsRun) => (

    expect.experimentRunId.equals(actual.experimentRunId) &&
    expect.experimentId.equals(actual.experimentId) &&
    expect.displayedLanguage.equals(actual.displayedLanguage) &&
    expect.aAdsUrl.equals(actual.aAdsUrl ) &&
    expect.aAdsText.equals(actual.aAdsText ) &&
    expect.aAdsShow.equals(actual.aAdsShow) &&
    expect.aAdsClick.equals(actual.aAdsClick) &&
    expect.bAdsUrl.equals(actual.bAdsUrl) &&
    expect.bAdsText.equals(actual.bAdsText ) &&
    expect.bAdsShow.equals(actual.bAdsShow) &&
    expect.bAdsClick.equals(actual.bAdsClick) &&
    expect.cAdsUrl.equals(actual.cAdsUrl) &&
    expect.cAdsText.equals(actual.cAdsText ) &&
    expect.cAdsShow.equals(actual.cAdsShow) &&
    expect.cAdsClick.equals(actual.cAdsClick) &&
    expect.dAdsUrl.equals(actual.dAdsUrl) &&
    expect.dAdsText.equals(actual.dAdsText ) &&
    expect.dAdsShow.equals(actual.dAdsShow) &&
    expect.dAdsClick.equals(actual.dAdsClick) &&
    expect.eAdsUrl.equals(actual.eAdsUrl) &&
    expect.eAdsText.equals(actual.eAdsText ) &&
    expect.eAdsShow.equals(actual.eAdsShow) &&
    expect.eAdsClick.equals(actual.eAdsClick) &&
    expect.fAdsUrl.equals(actual.fAdsUrl) &&
    expect.fAdsText.equals(actual.fAdsText ) &&
    expect.fAdsShow.equals(actual.fAdsShow) &&
    expect.fAdsClick.equals(actual.fAdsClick),
    "Same ExperimentAdsRun",
    if(!expect.experimentRunId.equals(actual.experimentRunId)) "experimentRunId is not equal"
    else if(!expect.experimentId.equals(actual.experimentId)) "experimentId is not equal"
    else if(!expect.displayedLanguage.equals(actual.displayedLanguage)) "displayedLanguage is not equal"
    else if(!expect.aAdsUrl .equals(actual.aAdsUrl )) "aAdsUrl  is not equal"
    else if(!expect.aAdsShow.equals(actual.aAdsShow)) "aAdsShow is not equal"
    else if(!expect.aAdsClick.equals(actual.aAdsClick)) "aAdsClick is not equal"
    else if(!expect.bAdsUrl.equals(actual.bAdsUrl)) "bAdsUrl is not equal"
    else if(!expect.bAdsShow.equals(actual.bAdsShow)) "bAdsShow is not equal"
    else if(!expect.bAdsClick.equals(actual.bAdsClick)) "bAdsClick is not equal"
    else if(!expect.cAdsUrl.equals(actual.cAdsUrl)) "cAdsUrl is not equal"
    else if(!expect.cAdsShow.equals(actual.cAdsShow)) "cAdsShow is not equal"
    else if(!expect.cAdsClick.equals(actual.cAdsClick)) "cAdsClick is not equal"
    else if(!expect.dAdsUrl.equals(actual.dAdsUrl)) "dAdsUrl is not equal"
    else if(!expect.dAdsShow.equals(actual.dAdsShow)) "dAdsShow is not equal"
    else if(!expect.dAdsClick.equals(actual.dAdsClick)) "dAdsClick is not equal"
    else if(!expect.eAdsUrl.equals(actual.eAdsUrl)) "eAdsUrl is not equal"
    else if(!expect.eAdsShow.equals(actual.eAdsShow)) "eAdsShow is not equal"
    else if(!expect.eAdsClick.equals(actual.eAdsClick)) "eAdsClick is not equal"
    else if(!expect.fAdsUrl.equals(actual.fAdsUrl)) "fAdsUrl is not equal"
    else if(!expect.fAdsShow.equals(actual.fAdsShow)) "fAdsShow is not equal"
    else if(!expect.fAdsClick.equals(actual.fAdsClick)) "fAdsClick is not equal"
    else "Ads text not matched"
  )

  def beSameUsageDuration(expect: UsageDuration) : Matcher[UsageDuration] = (actual: UsageDuration) => (

    actual.device_id.equals(expect.device_id) &&
    actual.data_date == expect.data_date &&
    actual.data_hour == expect.data_hour,
    "Be same usage duration",
    if(actual.duration_id == expect.duration_id) "duration_id is difference"
    else if(actual.device_id.equals(expect.device_id)) "device_id is difference"
    else if(actual.data_date == expect.data_date) "data_date is difference"
    else if(actual.data_hour == expect.data_hour) "data_hour is difference"
    else "unknown error"
    )

  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}
}
