package repositories

import java.lang.reflect.Field
import java.sql.{ResultSet, SQLException}
import java.util.UUID

import context.CoreContext
import org.joda.time.DateTime


/**
  *   * # Created by wacharint on 7/6/15.
  * */

trait InjectAble {

  val callContext : CoreContext
  var fields : Seq[Field]
  val tableName : String

  // database columns
  var recCreatedWhen: DateTime = DateTime.now()
  var recCreatedBy: UUID = null
  var recModifiedWhen: DateTime = null
  var recModifiedBy: UUID = null
  var recStatus: Int = 0

  def insert(): Unit ={

    var sqlStatement =
      """INSERT INTO %s
        |(#columns#)
        |VALUES
        |(#values#)""".stripMargin.format(tableName)

    fields = fields.filterNot(field => field.getName == "connection").
      filterNot(field => field.getName == "databaseUrl").
      filterNot(field => field.getName == "serialVersionUID").
      filterNot(field => field.getName == "callContext").
      filterNot(field => field.getName == "fields").
      filterNot(field => field.getName == "tableName").
      filterNot(field => field.getName == "recCreatedWhen").
      filterNot(field => field.getName == "recCreatedBy").
      filterNot(field => field.getName == "recModifiedWhen").
      filterNot(field => field.getName == "recModifiedBy")

    fields.foreach(field => {

      field.setAccessible(true)
      val value = field.get(this)
      if(value != null) {

        sqlStatement = sqlStatement.replace( """#columns#""", field.getName.replaceAll( """([A-Z])""", """_$1""").toLowerCase.concat( """,#columns#"""))
        sqlStatement = sqlStatement.replace( """#values#""", """'%s',#values#""".format(value.toString))
      }
    })

    sqlStatement = sqlStatement.replace( """#columns#""", """rec_created_by,#columns#""")
    sqlStatement = sqlStatement.replace( """#values#""", """'%s',#values#""".format(callContext.currentUserId.toString))
    sqlStatement = sqlStatement.replace( """#columns#""", """rec_created_when,#columns#""")
    sqlStatement = sqlStatement.replace( """#values#""", """'%s',#values#""".format(DateTime.now().toString()))

    sqlStatement = sqlStatement.replace(""",#columns#""", "")
    sqlStatement = sqlStatement.replace(""",#values#""", "")

    try {
      val conn = callContext.connection.get
      val statement = conn.createStatement()
      val res = statement.executeUpdate(sqlStatement)
    } catch {
      case e : SQLException => throw e
      case e : Exception => throw e
    }
  }

  def get(keyName : String, keyValue : String) : Seq[InjectAble] = {

    val sqlStatement = """SELECT * FROM %s_vu WHERE %s = '%s'""".format(tableName, keyName, keyValue)
    var hasData = false
    var returnedSeq: Seq[InjectAble] = Nil

    var res : ResultSet = null

    try {
      if(callContext.connection.isEmpty) {

        callContext.connect()
      }
      val conn = callContext.connection.get
      val statement = conn.createStatement()
      res = statement.executeQuery(sqlStatement)
    } catch {
      case e : SQLException => throw e
      case e : Exception => throw e
    }

    while(res.next) {

      fields = fields.filterNot(field => field.getName == "connection").
        filterNot(field => field.getName == "databaseUrl").
        filterNot(field => field.getName == "serialVersionUID").
        filterNot(field => field.getName == "callContext").
        filterNot(field => field.getName == "fields").
        filterNot(field => field.getName == "tableName")

      val result : InjectAble = this.getClass.getConstructor(classOf[CoreContext]).newInstance(callContext)

      fields.foreach( field => {

        field.setAccessible(true)
        val columnName = field.getName.replaceAll("""([A-Z])""", """_$1""").toLowerCase
        val value = res.getObject(columnName)
        if(value != null) {

          hasData = true

          field.getType.getSimpleName match {
            case "UUID" =>

              val typeValue = UUID.fromString(value.asInstanceOf[String])
              field.set(result, typeValue)

            case "DateTime" =>

              val stringVal = value.asInstanceOf[String]
              val typeValue = DateTime.parse(stringVal)
              field.set(result, typeValue)

            case _ =>

              field.set(result, value)
          }
        } else {

          field.set(result, null)
        }
      })

      returnedSeq = returnedSeq ++ Seq(result)
    }
    if(hasData) returnedSeq else Nil
  }
}

