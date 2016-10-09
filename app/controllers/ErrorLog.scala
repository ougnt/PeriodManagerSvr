package controllers

import context.CoreContext
import play.api.mvc.{Action, AnyContent, Controller, Request}
import repository.JsonSerializerImpl

/**
  * * # Created by wacharint on 10/9/2016 AD.
  **/
object ErrorLog extends Controller {

  implicit var overrrideContext: Option[CoreContext] = None

  def logError = Action { request: Request[AnyContent] => {

    implicit var context = if (overrrideContext isEmpty) new CoreContext else overrrideContext.get
    val requestJson = request.body.asJson.orNull

    if (requestJson == null) {

      Ok("Error: Request body = null")
    } else {

      val ser = new JsonSerializerImpl
      val log = ser.jsonToErrorLog(requestJson)
      val errorId = log.get.insert()
      Ok(errorId.toString)
    }
  }
  }
}
