package controllers

import com.fasterxml.jackson.core.JsonParseException
import context.CoreContext
import data.{RsaDecoder, RsaHelper}
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.mvc._
import repository.{UserInfo, RsaRepository}

/**
  * * # Created by wacharint on 7/1/16.
  **/
object LoginApi extends Controller {

  val LoginFailMessage = "Login Failed"
  var OverrideContext: Option[CoreContext] = None

  def handShake = Action {

    implicit val coreContext: CoreContext = if(OverrideContext.isEmpty) new CoreContext else OverrideContext.get

    val helper = new RsaHelper
    val decoder = helper.getRsaDecoder
    val encoder = helper.getRsaEncode

    val rsaData = new RsaRepository {

      d = decoder.d.toString(36)
      e = encoder.e.toString(36)
      n = encoder.n.toString(36)
    }

    val id = rsaData.insert()

    Ok("""{"id":"%s","e":"%s","n":"%s"}""".format(rsaData.rsaUuid, encoder.e.toString(36), encoder.n.toString(36)))
  }

  def login(id: String) = Action{ request: Request[AnyContent] => {

    implicit val coreContext: CoreContext = if(OverrideContext.isEmpty) new CoreContext else OverrideContext.get

    val rsaData = new RsaRepository().get(Seq(("rsa_uuid", id))).headOption.asInstanceOf[Option[RsaRepository]]
    var loginIsFail = false

    if(rsaData.isEmpty) {
      Ok(LoginFailMessage)
    } else {

      val decoder = new RsaDecoder(BigInt(rsaData.get.d, 36), BigInt(rsaData.get.n, 36))
      val textBody = request.body.asText

      if(textBody.isEmpty) {
        Ok(LoginFailMessage)
      } else {

        var decryptedMsg = ""

        try {
          decryptedMsg = decoder.decrypt(textBody.get)
        } catch {
          case e: NumberFormatException => loginIsFail = true
        }

        if(loginIsFail) {
          Ok(LoginFailMessage)
        } else {

          var jsValue: JsValue = null

          try {
            jsValue = Json.parse(decryptedMsg)
          } catch {
            case e: JsonParseException => loginIsFail = true
          }

          if(loginIsFail) {
            Ok(LoginFailMessage)
          } else {

            var userName = jsValue.asInstanceOf[JsObject].value.find(p => p._1.equalsIgnoreCase("userName")).getOrElse("" -> "\"\"")._2.toString
            var password = jsValue.asInstanceOf[JsObject].value.find(p => p._1.equalsIgnoreCase("password")).getOrElse("" -> "\"\"")._2.toString
            userName = userName.substring(1, userName.length - 1)
            password = password.substring(1, password.length - 1)

            val userInfo = new UserInfo().get(Seq(("user_email", userName), ("password", password))).headOption.asInstanceOf[Option[UserInfo]]

            if (userInfo.isEmpty) {
              Ok(LoginFailMessage)
            } else {
              Ok(userInfo.head.userToken.toString)
            }
          }
        }
      }
    }
  }}

}
