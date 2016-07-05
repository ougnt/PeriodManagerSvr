package repository

import java.lang.reflect.Field
import java.util.UUID

import context.CoreContext
import repositories.InjectAble

/**
  * * # Created by wacharint on 7/1/16.
  **/
class RsaRepository(implicit coreContext: CoreContext) extends InjectAble {

  var rsaId = 0l
  val rsaUuid = UUID.randomUUID
  var e = ""
  var d = ""
  var n = ""
  recStatus = 1

  override val callContext: CoreContext = coreContext
  override val tableName: String = "rsa_data"
  override var fields: Seq[Field] = classOf[RsaRepository].getDeclaredFields
}
