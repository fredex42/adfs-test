package controllers;

import org.pac4j.saml.profile.SAML2Profile;

public class GNMCustomProfile extends SAML2Profile {
    public String getJobTitle() { return this.getAttribute("job_title", String.class); }
}
