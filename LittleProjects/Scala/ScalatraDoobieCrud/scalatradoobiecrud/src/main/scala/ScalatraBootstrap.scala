import com.github.gr1f0n6x.scalatra.doobie.crud.servlet._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new IndexServlet, "/*")
    context.mount(new EntityServlet, "/entity/*")
  }

  override def destroy(context: ServletContext): Unit = super.destroy(context)
}
