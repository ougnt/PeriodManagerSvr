package controllers

import context.CoreContext
import org.joda.time.DateTime
import play.api.mvc._
import repositories.InjectAble
import repository.{DailyUsage, Device, JsonSerializer, JsonSerializerImpl}

object Application extends Controller {

  var overridedSerializer: Option[JsonSerializer] = None
  var overridedInjectables: Seq[InjectAble] = Nil

  val NoContentReturnMessage = """{"Result":"Error - No content"}"""
  val ResultOkReturnMessage = """{"Result":"Ok"}"""
  val InvalidResultReturnMessage = """{"Result":"Error - Invalid Content"}"""

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def updateStat = Action { request: Request[AnyContent] => {

    implicit val context = new CoreContext

    val reqJson = request.body.asJson.orNull
    var ret: Option[Result] = None

    val newSerializer = if(overridedSerializer.isEmpty) new JsonSerializerImpl else overridedSerializer.get
    val stat = newSerializer.jsonToUsageStatistics(reqJson)

    if(reqJson == null && ret.isEmpty) {
      ret = Some(Ok(NoContentReturnMessage))
    } else if(stat.isEmpty) {

      ret = Some(Ok(InvalidResultReturnMessage))
    } else if(ret.isEmpty){

      try {
        val device = new Device
        device.deviceId = stat.get.deviceId
        if(Integer.parseInt(stat.get.applicationVersion) >= 29) {

          device.language = stat.get.setting_displayed_language
        }
        device.insertOrUpdate(Seq(("device_id",device.deviceId toString)))

        stat.get.insertOrUpdate(
          Seq(("device_id", stat.get.deviceId.toString),
            ("application_version", stat.get.applicationVersion)))

        updateDailyStat(device, stat.get.applicationVersion)

        ret = Some(Ok(ResultOkReturnMessage))
      } catch {

        case e: Exception => {
          ret = Some(Ok("""{"Result":"Error - %s"}""".format(e.getMessage)))
        }
      }
    }

    ret.get
  }}

  def updateDailyStat(device: Device, appVersion: String)(implicit context: CoreContext) = {

    val currentDate = DateTime.now
    val criteria = Seq(
      ("device_id", device.deviceId.toString),
      ("data_hour", currentDate.getHourOfDay.toString),
      ("data_date", currentDate.toString("YYYY-MM-dd")))

    val usageInDbs = if(overridedInjectables == Nil)
      new DailyUsage().get(criteria).asInstanceOf[Seq[DailyUsage]]
    else
      overridedInjectables

    var usage = 0

    if(usageInDbs.isEmpty) {

      // Insert a new dailyUsage
      usage = 1
    } else {

      // add the counter
      usage = usageInDbs.head.asInstanceOf[DailyUsage].usageCounter + 1
    }

    val dailyUsage = new DailyUsage() {

      deviceId = device.deviceId
      applicationVersion = appVersion
      dataDate = currentDate.toString("YYYY-MM-dd")
      dataHour = currentDate.getHourOfDay
      usageCounter = usage
    }

    dailyUsage.insertOrUpdate(criteria)
  }
}