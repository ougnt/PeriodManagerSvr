
import context.CoreContext
import org.specs2.mutable.Specification
import org.specs2.specification.Fragments

/**
  * * # Created by wacharint on 3/3/16.
  **/
abstract trait BasedSpec extends Specification {

  implicit val context = new CoreContext

  override def map(fragments: => Fragments) = {
    sequential ^
      step(ultimateBeforeAll()) ^
      step(beforeAll()) ^
      fragments ^
      step(afterAll()) ^
      step(ultimateAfterAll())
  }

  private def ultimateBeforeAll() = {

    context.connect()
    context.connection.get.setAutoCommit(false)
  }

  private def ultimateAfterAll() = {

    context.connection.get.rollback()
  }

  protected def beforeAll()
  protected def afterAll()
}
