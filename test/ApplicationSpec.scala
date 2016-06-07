import controllers.Application
import org.joda.time.DateTime
import org.junit.runner._
import org.specs2.mock.Mockito
import org.specs2.mutable.BeforeAfter
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._
import repository.{DailyUsage, Device}

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends BasedSpec with BeforeAfter with Mockito {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Your new application is ready.")
    }
  }

  """Application updateDailyStat""" should {

    """be able to save data when it is the first use of the day""" in {

      // Setting
      val device = new Device
      device.insert()
      Application.overridedInjectables = Nil

      // Execute
      Application.updateDailyStat(device, "1")

      // Verify
      val retUsage = new DailyUsage().get(Seq(
        ("device_id", device.deviceId.toString),
        ("data_date", DateTime.now.toString("YYYY-MM-dd"))))

      retUsage.size mustEqual 1
      retUsage.head.asInstanceOf[DailyUsage].usageCounter mustEqual 1
      retUsage.head.asInstanceOf[DailyUsage].dataHour mustEqual DateTime.now.hourOfDay.get()
    }

    """be able to save data and increase the count when it is the second time of use of the day""" in {

      // Setting
      val device = new Device
      device.insert()
      Application.overridedInjectables = Nil

      // Execute
      Application.updateDailyStat(device, "1")
      Application.updateDailyStat(device, "1")

      // Verify
      val retUsage = new DailyUsage().get(Seq(
        ("device_id", device.deviceId.toString),
        ("data_date", DateTime.now.toString("YYYY-MM-dd"))))

      retUsage.size mustEqual 1
      retUsage.head.asInstanceOf[DailyUsage].usageCounter mustEqual 2
      retUsage.head.asInstanceOf[DailyUsage].dataHour mustEqual DateTime.now.hourOfDay.get
    }
  }

  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}

  override def after: Any = {

  }

  override def before: Any = {

  }
}
