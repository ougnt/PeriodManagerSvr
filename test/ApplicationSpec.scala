import java.util.UUID

import controllers.{LoginApi, Application}
import data.RsaEncoder
import org.joda.time.DateTime
import org.junit.runner._
import org.specs2.mock.Mockito
import org.specs2.mutable.BeforeAfter
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._
import repository.{User, UserInfo, DailyUsage, Device}

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

  """Application Login""" should {

    """return the user token when login success""" in {

      // setup
      LoginApi.OverrideContext = Some(context)
      val user = new User {
        descr = "temp user"
      }

      user.insert()

      val userInfo = new UserInfo() {

        userId = user.userId
        userToken = UUID.randomUUID
        userEmail = "temp@testmua.com"
        password = "abcdefgh"
      }
      userInfo.insert

      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"userName":"%s","password":"%s"}""".format(userInfo.userEmail, userInfo.password))

      // execute
      val loginRet = LoginApi.login(id)(FakeRequest(POST, "/login").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val loginResult = contentAsString(loginRet)
      loginResult mustEqual userInfo.userToken.toString
    }

    """return login fail message when the username is not exist""" in {

      // setup
      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"userName":"%s","password":"%s"}""".format("Notexisting@fake.com", ""))

      // execute
      val loginRet = LoginApi.login(id)(FakeRequest(POST, "/login").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val loginResult = contentAsString(loginRet)
      loginResult mustEqual LoginApi.LoginFailMessage
    }

    """return login fail message when the password is not matched""" in {

      // setup
      LoginApi.OverrideContext = Some(context)
      val user = new User {
        descr = "temp user"
      }

      user.insert()

      val userInfo = new UserInfo() {

        userId = user.userId
        userToken = UUID.randomUUID
        userEmail = "temp1@testmua.com"
        password = "abcdefgh"
      }
      userInfo.insert

      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"userName":"%s","password":"%s"}""".format(userInfo.userEmail, "ddd"))

      // execute
      val loginRet = LoginApi.login(id)(FakeRequest(POST, "/login").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val loginResult = contentAsString(loginRet)
      loginResult mustEqual LoginApi.LoginFailMessage
    }

    """return login fail message when incorrect encrypt key""" in {

      // setup
      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"userName":"%s","password":"%s"}""".format("Notexisting@fake.com", ""))

      // execute
      val loginRet = LoginApi.login(UUID.randomUUID().toString)(FakeRequest(POST, "/login").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val loginResult = contentAsString(loginRet)
      loginResult mustEqual LoginApi.LoginFailMessage
    }

    """return login fail message when incorrect form of json payload""" in {

      // setup
      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"user Name":"%s","pass word":"%s"}""".format("Notexisting@fake.com", ""))

      // execute
      val loginRet = LoginApi.login(id)(FakeRequest(POST, "/login").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val loginResult = contentAsString(loginRet)
      loginResult mustEqual LoginApi.LoginFailMessage
    }

    """return login fail message when payload is not json""" in {

      // setup
      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""userName|%s|password"%s|""".format("Notexisting@fake.com", ""))

      // execute
      val loginRet = LoginApi.login(id)(FakeRequest(POST, "/login").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val loginResult = contentAsString(loginRet)
      loginResult mustEqual LoginApi.LoginFailMessage
    }
  }

  """Application Register""" should {

    """return the user token when register success""" in {

      // setup
      LoginApi.OverrideContext = Some(context)

      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"email":"%s","password":"%s"}""".format("email@email.com", "password"))

      // execute
      val registerRet = LoginApi.register(id)(FakeRequest(POST, "/register").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val registerResult = contentAsString(registerRet)
      val expectedRegisteredUser = new UserInfo().get(Seq("user_email" -> "email@email.com", "password" -> "password"))
      expectedRegisteredUser.size mustEqual 1
    }

    """return the register fail when incorrect encrypt key""" in {

      // setup
      LoginApi.OverrideContext = Some(context)

      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"email":"%s","password":"%s"}""".format("email@email.com", "password"))

      // execute
      val registerRet = LoginApi.register(UUID.randomUUID().toString)(FakeRequest(POST, "/register").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val registerResult = contentAsString(registerRet)
      registerResult mustEqual LoginApi.RegisterFailMessage
    }

    """return the register fail when invalid json""" in {

      // setup
      LoginApi.OverrideContext = Some(context)

      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"email":"%s""password":"%s"}""".format("email@email.com", "password"))

      // execute
      val registerRet = LoginApi.register(id)(FakeRequest(POST, "/register").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val registerResult = contentAsString(registerRet)
      registerResult mustEqual LoginApi.RegisterFailMessage
    }

    """return the register fail when the email is existing""" in {

      // setup
      LoginApi.OverrideContext = Some(context)
      val user = new User {
        descr = "temp user"
      }

      user.insert()

      val userInfo = new UserInfo() {

        userId = user.userId
        userToken = UUID.randomUUID
        userEmail = "temp2@testmua.com"
        password = "abcdefgh"
      }
      userInfo.insert

      val handShakeRet = LoginApi.handShake(FakeRequest(GET, "/handshake").withHeaders("Content-Type" -> "Application/Json"))

      val handShakeJson = contentAsJson(handShakeRet)
      val id = (handShakeJson \ "id" toString).stripPrefix("\"").stripSuffix("\"")
      val e = (handShakeJson \ "e" toString).stripPrefix("\"").stripSuffix("\"")
      val n = (handShakeJson \ "n" toString).stripPrefix("\"").stripSuffix("\"")
      val encryptor = new RsaEncoder(BigInt(e, 36), BigInt(n, 36))
      val encryptedMsg = encryptor.encrypt("""{"email":"%s","password":"%s"}""".format(userInfo.userEmail, "password"))

      // execute
      val registerRet = LoginApi.register(id)(FakeRequest(POST, "/register").
        withHeaders("Content-Type" -> "text/plain").
        withTextBody(encryptedMsg))

      // verify
      val registerResult = contentAsString(registerRet)
      registerResult mustEqual LoginApi.EmailExisting
    }
  }

  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}

  override def after: Any = {

  }

  override def before: Any = {

  }
}
