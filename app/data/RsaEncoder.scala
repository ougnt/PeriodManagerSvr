package data

/**
  * * # Created by wacharint on 6/30/16.
  **/
class RsaEncoder(val e: BigInt, val n: BigInt) {

  def encrypt(message: String): String = {

    var results = ""
    message.foreach(c => results = results + encrypt(c).toString(36) + "|")
    results = results.substring(0, results.size - 1)
    results
  }

  private def encrypt(message: Int): BigInt = {

    BigInt(message).modPow(e, n)
  }

  private def encrypt(char: Char): BigInt = {

    encrypt(char.toByte.toInt)
  }
}
