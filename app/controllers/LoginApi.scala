package controllers

import java.sql.SQLException

import com.fasterxml.jackson.core.JsonParseException
import context.CoreContext
import data.{RsaEncoder, RsaDecoder, RsaHelper}
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.mvc._
import repository.{User, UserInfo, RsaRepository}

/**
  * * # Created by wacharint on 7/1/16.
  **/
object LoginApi extends Controller {

  val LoginFailMessage = "Login Failed"
  val RegisterFailMessage = "Register Failed"
  val EmailExistingMessage = "Email Existing"
  val BadRequestMessage = "Bad Request"
  val OkMessage = "Ok"
  var OverrideContext: Option[CoreContext] = None

  val encoder = new RsaEncoder(BigInt("nz", 36), BigInt("aq7k03xk4ouvzsrktw2lasctp", 36))

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

    val decryptedMg = decryptMsg(id, request)
    var loginIsFail = false

    if(decryptedMg.equals(BadRequestMessage) ){
      Ok(BadRequestMessage)
    } else {

      var jsValue: JsValue = null

      try {
        jsValue = Json.parse(decryptedMg)
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

        val userInfo = new UserInfo().get(Seq(("user_email", userName), ("password", encoder.encrypt(password)))).headOption.asInstanceOf[Option[UserInfo]]

        if (userInfo.isEmpty) {
          Ok(LoginFailMessage)
        } else {
          Ok(userInfo.head.userToken.toString)
        }
      }
    }
  }}

  def register(id: String) = Action{ request: Request[AnyContent] => {

    implicit val coreContext: CoreContext = if(OverrideContext.isEmpty) new CoreContext else OverrideContext.get
    var registerIsFail = false

    val decryptedMsg = decryptMsg(id, request)
    if(decryptedMsg.equals(BadRequestMessage)) {
      Ok(BadRequestMessage)
    } else {

      var reqJson: JsValue = null

      try {

        reqJson = Json.parse(decryptedMsg)
      } catch {
        case e: JsonParseException => registerIsFail = true
        case e: java.lang.Exception => registerIsFail = true
      }

      if(registerIsFail) {

        Ok(RegisterFailMessage)
      } else {

        var email = reqJson.asInstanceOf[JsObject].value.find( p => p._1.equalsIgnoreCase("email")).getOrElse("" -> "\"\"")._2.toString
        var passwd = reqJson.asInstanceOf[JsObject].value.find( p => p._1.equalsIgnoreCase("password")).getOrElse("" -> "\"\"")._2.toString

        email = email.substring(1, email.length -1)
        passwd = passwd.substring(1, passwd.length -1)

        if(new UserInfo().get(Seq("user_email" -> email)).size > 0) {

          Ok(EmailExistingMessage)
        } else if(email.length < 3 || passwd.length < 3) {

          Ok(RegisterFailMessage)
        } else {

          val newUser = new User() {
            descr = "Registered user"
          }
          val newUserInfo = new UserInfo() {

            userId = newUser.userId
            userEmail = email
            password = encoder.encrypt(passwd)
          }

          try {

            newUser.insert()
            newUserInfo.insert()
          } catch {
            case e: SQLException => {
              val msg = e.getMessage()
              registerIsFail = true
            }
          }

          if(registerIsFail) {

            Ok(RegisterFailMessage)
          } else {

            Ok(newUserInfo.userToken.toString)
          }
        }
      }
    }
  } }

  def decryptMsg(id: String, request: Request[AnyContent])(implicit coreContext: CoreContext): String = {

    val rsaData = new RsaRepository().get(Seq(("rsa_uuid", id))).headOption.asInstanceOf[Option[RsaRepository]]
    var decryptFail = false

    if (rsaData.isEmpty) {
      BadRequestMessage
    } else {

      val decoder = new RsaDecoder(BigInt(rsaData.get.d, 36), BigInt(rsaData.get.n, 36))
      val textBody = request.body.asText

      if (textBody.isEmpty) {
        BadRequestMessage
      } else {

        var decryptedMsg = ""

        try {
          decryptedMsg = decoder.decrypt(textBody.get)
        } catch {
          case e: NumberFormatException => decryptFail = true
        }

        if(decryptFail) {
          BadRequestMessage
        } else {
          decryptedMsg
        }
      }
    }
  }
}
