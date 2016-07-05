package data

/**
  * * # Created by wacharint on 6/30/16.
  **/
class RsaDecoder(val d: BigInt, val n: BigInt) {

  def decrypt(message: String): String = {

    var results = ""
    message.split("""\|""").foreach(c =>
      results = results + decryptToChar(BigInt(c, 36))
    )
    results = results.substring(0, results.size)
    results
  }

  private def decrypt(encryptedMessage: BigInt): Int = {

    encryptedMessage.modPow(d, n).toInt
  }

  private def decryptToChar(bigInt: BigInt): Char = {

    decrypt(bigInt).asInstanceOf[Char]
  }
}
