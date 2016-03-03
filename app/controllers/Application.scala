package controllers

import context.CoreContext
import play.api.mvc._
import repository.{JsonSerializer, JsonSerializerImpl}

object Application extends Controller {

  var overridedSerializer: Option[JsonSerializer] = None

  val NoContentReturnMessage = """{"Result":"Error - No content"}"""
  val ResultOkReturnMessage = """{"Result":"Ok"}"""
  val InvalidResultReturnMessage = """{"Result":"Error - Invalid Content"}"""

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def updateStat = Action { request: Request[AnyContent] => {

    implicit val context = new CoreContext

    val reqJson = request.body.asJson.getOrElse(null)
    var ret: Option[Result] = None

    val newSerializer = if(overridedSerializer.isEmpty) new JsonSerializerImpl else overridedSerializer.get
    val stat = newSerializer.jsonToUsageStatistics(reqJson)

    if(reqJson == null && ret.isEmpty) {
      ret = Some(Ok(NoContentReturnMessage))
    } else if(stat.isEmpty) {

      ret = Some(Ok(InvalidResultReturnMessage))
    } else if(ret.isEmpty){

      try {
        stat.get.insertOrUpdate(
          Seq(("device_id", stat.get.deviceId.toString),
            ("application_version", stat.get.applicationVersion)))

        ret = Some(Ok(ResultOkReturnMessage))
      } catch {

        case e: Exception => {
          ret = Some(Ok("""{"Result":"Error - %s"}""".format(e.getMessage)))
        }
      }
    }

    ret.get
  }}

}