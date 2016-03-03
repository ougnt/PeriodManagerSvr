import java.util.UUID

import context.CoreContext
import controllers.Application
import org.specs2.mock.Mockito
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeRequest, PlaySpecification}
import repository.{JsonSerializer, UsageStatistics}

/**
  * * # Created by wacharint on 2/29/16.
  **/
class ActionSpec extends PlaySpecification with Mockito with BasedSpec {

  """APIs""" should {

    """response 200 when the statistics is valid""" in {

      // Setup
      val mockSerializer = mock[JsonSerializer]
      mockSerializer.jsonToUsageStatistics(any[JsValue])(any[CoreContext]) returns Some(new UsageStatistics())
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

  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}
}
