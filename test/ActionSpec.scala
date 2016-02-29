import controllers.Application
import play.api.test.{FakeRequest, PlaySpecification}

/**
  * * # Created by wacharint on 2/29/16.
  **/
class ActionSpec extends PlaySpecification {



  """APIs""" should {

    """response 200 when the statistics is valid""" in {

      // Setup

      // Execute
      val ret = Application.updateStat(FakeRequest(POST,"/usageStat")
        .withHeaders("Content-Type" -> "application/text")
          .withTextBody("ss"))

      val retText = contentAsString(ret)

      // Verify
      retText mustEqual "ss"
    }
  }
}
