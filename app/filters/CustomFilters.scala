package filters

import javax.inject.Inject
import org.pac4j.play.filters.SecurityFilter
import play.api.http.{DefaultHttpFilters, HttpFilters}
import play.api.mvc.EssentialFilter

class CustomFilters @Inject()(securityFilter:SecurityFilter) extends DefaultHttpFilters(securityFilter)
