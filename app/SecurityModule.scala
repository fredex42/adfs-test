import java.security.KeyPair
import java.util.Base64

import com.google.inject.{AbstractModule, Provides}
import com.nimbusds.jose.jwk.JWK
import controllers.DemoHttpActionAdapter
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.Clients
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.core.context.HttpConstants.HTTP_METHOD
import org.pac4j.core.matching.matcher._
import org.pac4j.http.client.direct.{DirectBasicAuthClient, DirectBearerAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.jwt.config.signature.{ECSignatureConfiguration, RSASignatureConfiguration}
import org.pac4j.play.scala.{DefaultSecurityComponents, SecurityComponents}
import org.pac4j.play.{CallbackController, LogoutController}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import play.api.{Configuration, Environment}

import scala.jdk.CollectionConverters._

class SecurityModule(environment: Environment, config:Configuration) extends AbstractModule {
  lazy val baseUrl = config.get[String]("baseUrl")

  override def configure(): Unit = {
    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])

    //login callback
    val callbackController = new CallbackController()

    callbackController.setDefaultUrl("/")
    callbackController.setMultiProfile(true)
    callbackController.setDefaultClient("FormClient")
    bind(classOf[CallbackController]).toInstance(callbackController)

    //logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/loggedout")
    bind(classOf[LogoutController]).toInstance(logoutController)

    //security components used in controllers
    bind(classOf[SecurityComponents]).to(classOf[DefaultSecurityComponents])
  }

  @Provides
  def provideDirectBasicAuthClient: DirectBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator)

  @Provides
  def provideFormClient:FormClient = new FormClient("/loginform","uid","passwd",new SimpleTestUsernamePasswordAuthenticator)

  @Provides
  def provideDirectBearerAuthClient:DirectBearerAuthClient = {
    //if anything raises just let it crash, for the time being....
    val jwk = JWK.parseFromPEMEncodedX509Cert(config.get[String]("auth.signingCertPem"))

    val keypair = new KeyPair(jwk.toRSAKey.toPublicKey, null)
    val sigConfig = new RSASignatureConfiguration(keypair)
    new DirectBearerAuthClient(new JwtAuthenticator(sigConfig))
  }

  @Provides
  def provideConfig(formClient: FormClient, directBasicAuthClient: DirectBasicAuthClient, bearerClient:DirectBearerAuthClient): Config = {

    val clients = new Clients(baseUrl + "/callback", formClient,  directBasicAuthClient, bearerClient, new AnonymousClient())

    val config = new Config(clients)
    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer[Nothing]("ROLE_ADMIN"))

    config.addMatcher("loginform", new PathMatcher().excludePath("/loginform"))
    config.setHttpActionAdapter(new DemoHttpActionAdapter())

    new HttpMethodMatcher(HTTP_METHOD.OPTIONS)
    config.addMatcher("api", new PathMatcher().excludePath("/api"))
    config
  }
}
