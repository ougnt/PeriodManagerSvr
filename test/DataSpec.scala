import data.RsaHelper

/**
  * * # Created by wacharint on 6/28/16.
  **/
class DataSpec extends BasedSpec {

  """RsaHelper""" should {

    """be able to encode data and recode it back""" in {

      // Setup
      val helper = new RsaHelper
      val encoder = helper.getRsaEncode
      val decoder = helper.getRsaDecoder
      val msg =
        """{"Test":"Hello , "eee":123}""".stripMargin
      val encryptedMsg = encoder.encrypt(msg)

      // Execute
      val decryptedMsg = decoder.decrypt(encryptedMsg)

      // Verify
      decryptedMsg mustEqual msg
    }
  }

  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}
}
