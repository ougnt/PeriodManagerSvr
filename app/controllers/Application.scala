package controllers

import java.util.UUID

import context.CoreContext
import org.joda.time.DateTime
import play.api.mvc._
import repositories.InjectAble
import repository._

import scala.util.Random

object Application extends Controller {

  var overridedSerializer: Option[JsonSerializer] = None
  var overridedInjectables: Seq[InjectAble] = Nil
  var overridedRandom: Option[Random] = None

  val NoContentReturnMessage = """{"Result":"Error - No content"}"""
  val ResultOkReturnMessage = """{"Result":"Ok"}"""
  val InvalidResultReturnMessage = """{"Result":"Error - Invalid Content"}"""

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def adsAsk(deviceId: String, language: String) = Action {

    implicit val context: CoreContext = new CoreContext

    insertDeviceIfNotExists(UUID.fromString(deviceId), language)

    val experimentRun = getExperimentRun(UUID.fromString(deviceId), language)
    val randomExp = getRandomExperimentUrl(experimentRun)

    Ok(
      """{"AdsUrl":"%s","AdsText":"%s", "experimentRunId":"%s", "experimentUser":"%s"}"""
        .format(randomExp._1, randomExp._2, randomExp._3, randomExp._4  )
    )
  }

  def adsClick(experimentRunId: Int, user: String) = Action {

    implicit val context: CoreContext = new CoreContext
    val experimentRun: ExperimentAdsRun = if(overridedInjectables.isEmpty)
      new ExperimentAdsRun().get(Seq(("experiment_run_id", experimentRunId.toString))).head.asInstanceOf[ExperimentAdsRun]
    else
      overridedInjectables.head.asInstanceOf[ExperimentAdsRun]

    if(experimentRun == null) {

      Ok("Error")
    } else {

      user match {
        case "a" => experimentRun.aAdsClick = experimentRun.aAdsClick + 1
        case "b" => experimentRun.bAdsClick = experimentRun.bAdsClick + 1
        case "c" => experimentRun.cAdsClick = experimentRun.cAdsClick + 1
        case "d" => experimentRun.dAdsClick = experimentRun.dAdsClick + 1
        case "e" => experimentRun.eAdsClick = experimentRun.eAdsClick + 1
        case "f" => experimentRun.fAdsClick = experimentRun.fAdsClick + 1
      }
      experimentRun.insertOrUpdate(Seq(("experiment_run_id", experimentRunId.toString)))
      Ok("Ok")
    }
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

        insertDuration(device.deviceId, stat.get)

        ret = Some(Ok(ResultOkReturnMessage))
      } catch {

        case e: Exception => {
          val x = e.getMessage
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

  def insertDeviceIfNotExists(deviceId: UUID, language: String)(implicit context: CoreContext): Unit = {

    val device = if(overridedInjectables.isEmpty) new Device() else overridedInjectables.head.asInstanceOf[Device]
    if (device.get(Seq(("device_id", deviceId.toString))).isEmpty) {

      device.deviceId = deviceId
      device.recStatus = 1
      device.language = language
      device.insert()
    }
  }

  def insertDuration(deviceId: UUID, stat: UsageStatistics)( implicit context: CoreContext) = {

    val duration = if(overridedInjectables isEmpty) new UsageDuration {
      device_id = deviceId
      data_date = stat.recCreatedWhen.toString("yyyy-MM-dd")
      data_hour = stat.recCreatedWhen.getHourOfDay
      duration = stat.duration
    } else overridedInjectables.head

    duration.insert()
  }

  def getExperimentRun(deviceId: UUID, language: String)(implicit context: CoreContext): ExperimentAdsRun = {

    val res = new ExperimentAdsRun().get(Seq(("rec_status","1"),("displayed_language",language))).asInstanceOf[Seq[ExperimentAdsRun]]
    if(res.isEmpty)
      new ExperimentAdsRun()
    else
      res.head
  }

  def getRandomExperimentUrl(experimentAdsRun: ExperimentAdsRun)(implicit context: CoreContext): (String, String, Int, Char) = {

    val random = if(overridedRandom.isEmpty) new Random else overridedRandom.get
    val randomNumber = random.nextInt(6)
    var retUrl = ""
    var retText = ""
    var experimentUser: Char = Char.MinValue

    if(randomNumber.equals(0)) {

      experimentAdsRun.aAdsShow = experimentAdsRun.aAdsShow + 1
      retUrl = experimentAdsRun.aAdsUrl
      retText = experimentAdsRun.aAdsText
      experimentUser = 'a'
    } else if(randomNumber.equals(1)) {

      experimentAdsRun.bAdsShow = experimentAdsRun.bAdsShow + 1
      retUrl = experimentAdsRun.bAdsUrl
      retText = experimentAdsRun.bAdsText
      experimentUser = 'b'
    } else if(randomNumber.equals(2)) {

      experimentAdsRun.cAdsShow = experimentAdsRun.cAdsShow + 1
      retUrl = experimentAdsRun.cAdsUrl
      retText = experimentAdsRun.cAdsText
      experimentUser = 'c'
    } else if(randomNumber.equals(3)) {

      experimentAdsRun.dAdsShow = experimentAdsRun.dAdsShow + 1
      retUrl = experimentAdsRun.dAdsUrl
      retText = experimentAdsRun.dAdsText
      experimentUser = 'd'
    } else if(randomNumber.equals(4)) {

      experimentAdsRun.eAdsShow = experimentAdsRun.eAdsShow + 1
      retUrl = experimentAdsRun.eAdsUrl
      retText = experimentAdsRun.eAdsText
      experimentUser = 'e'
    } else {

      experimentAdsRun.fAdsShow = experimentAdsRun.fAdsShow + 1
      retUrl = experimentAdsRun.fAdsUrl
      retText = experimentAdsRun.fAdsText
      experimentUser = 'f'
    }

    if(!experimentAdsRun.aAdsUrl.equals("")) {
      experimentAdsRun.insertOrUpdate(Seq(("experiment_run_id", experimentAdsRun.experimentRunId.toString)))
    }

    (retUrl, retText, experimentAdsRun.experimentRunId, experimentUser)
  }
}