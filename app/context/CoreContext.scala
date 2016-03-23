package context

import java.sql.{Connection, DriverManager, SQLException}
import java.util.UUID

/**
  * Created by wacharint on 7/12/15.
  *
  */
class CoreContext {

  var connection: Option[Connection] = None
  var url: String = "jdbc:mysql://localhost:3306/period_manager?user=root&password=Apibkk123*"
  var currentUserId: UUID = UUID.fromString("""a9998ce6-da2d-11e5-b5d2-0a1d41d68578""")

  def connect() : Unit = {

    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance
      connection = if (connection.isEmpty || connection.get.isClosed)
        Option(DriverManager.getConnection(url))
      else
        connection
    } catch {
      case e: SQLException => println("DEBUG : Got SQLException with message : %s".format(e.getMessage)); throw e
      case e: Exception => throw e
    }
  }
}
