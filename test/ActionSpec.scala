import java.util.UUID

import context.CoreContext
import controllers.Application
import org.specs2.mock.Mockito
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeRequest, PlaySpecification}
import repository._

import scala.util.Random

/**
  * * # Created by wacharint on 2/29/16.
  **/
class ActionSpec extends PlaySpecification with Mockito with BasedSpec {

  """APIs""" should {

    """response 200 when the statistics is valid""" in {

      // Setup
      val mockSerializer = mock[JsonSerializer]
      mockSerializer.jsonToUsageStatistics(any[JsValue])(any[CoreContext]) returns Some(new UsageStatistics(){applicationVersion = "29"})
      Application.overridedSerializer = Some(mockSerializer)

      // Execute
      val ret = Application.updateStat(FakeRequest(POST,"/usageStat")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse("""{"Mock":"Mock"}""")))

      val retText = contentAsString(ret)

      // Verify
      retText mustEqual Application.ResultOkReturnMessage
    }

    """response Invalid content when the statistics is invalid UsageStatistics""" in {

      // Setup
      val mockSerializer = mock[JsonSerializer]
      mockSerializer.jsonToUsageStatistics(any[JsValue])(any[CoreContext]) returns None
      Application.overridedSerializer = Some(mockSerializer)

      // Execute
      val ret = Application.updateStat(FakeRequest(POST,"/usageStat")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse("""{"mock":"Mock"}""")))

      val retText = contentAsString(ret)

      // Verify
      retText mustEqual Application.InvalidResultReturnMessage
    }

    """response error when empty content""" in {

      // Setup
      Application.overridedSerializer = None

      // Execute
      val ret = Application.updateStat(FakeRequest(POST,"/usageStat")
        .withHeaders("Content-Type" -> "application/json"))

      val retText = contentAsString(ret)

      // Verify
      retText mustEqual Application.NoContentReturnMessage
    }

    """response error when no content""" in {

      // Setup
      Application.overridedSerializer = None

      // Execute
      val ret = Application.updateStat(FakeRequest(POST,"/usageStat")
        .withHeaders("Content-Type" -> "application/json"))

      val retText = contentAsString(ret)

      // Verify
      retText mustEqual Application.NoContentReturnMessage
    }

    """response error when got Exception""" in {

      // Setup
      val mockInjectAble = mock[UsageStatistics]
      val mockSerializer = mock[JsonSerializer]
      mockInjectAble.deviceId returns UUID.randomUUID()
      mockInjectAble.applicationVersion returns "1"

      mockInjectAble.insertOrUpdate(any) throws new RuntimeException("XXX")

      mockSerializer.jsonToUsageStatistics(any[JsValue])(any[CoreContext]) returns Some(mockInjectAble)

      Application.overridedSerializer = Some(mockSerializer)

      // Execute
      val ret = Application.updateStat(FakeRequest(POST,"/usageStat")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse("""{"Mock":"Mock"}""")))

      val retText = contentAsString(ret)

      // Verify
      retText mustEqual """{"Result":"Error - XXX"}"""
    }
  }

  """insertDeviceIfNotExists""" should {

    """insert a device if it is not exists""" in {

      // Setup
      val mockDevice = mock[Device]
      var isCalled = false
      mockDevice.get(any[Seq[(String,String)]]) returns Nil
      mockDevice.insert().answers((any) => {
        {
          isCalled = true
          11
        }
      })
      Application.overridedInjectables = Seq(mockDevice)

      // Execute
      Application.insertDeviceIfNotExists(UUID.randomUUID(), "ss")

      // Verify
      isCalled mustEqual true
    }

    """not insert a device if it is not exists""" in {

      // Setup
      val mockDevice = mock[Device]
      var isCalled = false
      mockDevice.get(any[Seq[(String,String)]]) returns Seq(new Device)
      mockDevice.insert().answers((any) => {
        isCalled = true
        0
      })
      Application.overridedInjectables = Seq(mockDevice)

      // Execute
      Application.insertDeviceIfNotExists(UUID.randomUUID(), "ss")

      // Verify
      isCalled mustEqual false
    }
  }

  """getRandomExperimentUrl""" should {

    """return an url of the experiment is running""" in {

      // Setup
      val mockRandom = mock[Random]
      mockRandom.nextInt(anyInt) returns 0
      Application.overridedRandom = Some(mockRandom)
      val newExperimentRun = mock[ExperimentAdsRun]
      newExperimentRun.aAdsUrl returns "test"
      newExperimentRun.aAdsText returns "Hello"
      newExperimentRun.experimentId returns 10
      newExperimentRun.experimentRunId returns 11


      // Execute
      val res = Application.getRandomExperimentUrl(newExperimentRun)

      // Verify
      res._1 mustEqual "test"
      res._2 mustEqual "Hello"
      res._3 mustEqual 11
      res._4 mustEqual 'a'
    }
  }

  """insertDuration""" should {

    """be able to insert all fields""" in {

      // Setup
      var insertFunctionIsCalled = false
      val mockDuration = mock[UsageDuration]
      mockDuration.insert answers( (Any) => {
        insertFunctionIsCalled = true
        1
      })
      Application.overridedInjectables = Seq(mockDuration)

      // Execute
      Application.insertDuration(UUID.randomUUID(), new UsageStatistics)

      // Verify
      insertFunctionIsCalled mustEqual true
    }
  }

  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}
}
