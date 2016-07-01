package data

import scala.util.Random

/**
  * * # Created by wacharint on 6/28/16.
  **/
class RsaHelper(pComponent: BigInt, qComponent: BigInt, eComponent: BigInt) {

  def this() = this(null,null,null)

  val rand = new Random
  val p: BigInt = if(pComponent == null) BigInt.probablePrime(20, rand) else pComponent
  val q: BigInt = if(qComponent == null) BigInt.probablePrime(20, rand) else qComponent
  val n: BigInt = p * q
  val phiN: BigInt = (p - 1) * (q - 1)
  val e: BigInt = if(eComponent == null) {
    var candidate = BigInt.probablePrime(10, rand)
    while(candidate < phiN && candidate.gcd(BigInt(1)) > 1) {
      candidate = candidate + 1
    }
    candidate
  } else eComponent


  val d: BigInt = e.modInverse(phiN)

  def getRsaEncode: RsaEncoder = new RsaEncoder(e, n)

  def getRsaDecoder: RsaDecoder = new RsaDecoder(d, n)

}
