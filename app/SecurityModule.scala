import com.google.inject.{AbstractModule, Provides}
import controllers.{CustomAuthorizer, DemoHttpActionAdapter}
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.Clients
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.core.matching.matcher.PathMatcher
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.play.scala.{DefaultSecurityComponents, SecurityComponents}
import org.pac4j.play.{CallbackController, LogoutController}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import org.pac4j.saml.client.SAML2Client
import org.pac4j.saml.config.SAML2Configuration
import play.api.{Configuration, Environment}

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
  def provideSaml2Client:SAML2Client = new SAML2Client(new SAML2Configuration(
    config.get[String]("saml2.keystorePath"),
    config.get[String]("saml2.keystorePassword"),
    config.get[String]("saml2.privateKeyPassword"),
    config.get[String]("saml2.identityProviderMetadataPath")
  ))

  @Provides
  def provideConfig(formClient: FormClient, indirectBasicAuthClient: IndirectBasicAuthClient,saml2Client: SAML2Client,
                    parameterClient: ParameterClient, directBasicAuthClient: DirectBasicAuthClient): Config = {

    val clients = new Clients(baseUrl + "/callback", formClient,  directBasicAuthClient, new AnonymousClient())

    val config = new Config(clients)
    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer[Nothing]("ROLE_ADMIN"))
    config.addAuthorizer("custom", new CustomAuthorizer)

    config.addMatcher("loginform", new PathMatcher().excludePath("/loginform"))
    config.addMatcher("excludedPath", new PathMatcher().excludeRegex("^/filter/facebook/notprotected\\.html$"))
    config.setHttpActionAdapter(new DemoHttpActionAdapter())

    config
  }
}
