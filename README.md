# adfs-test

A working proof-of-concept for ADFS/SAML auth for Play against ADFS

## What it is

This is a testing ground for better understanding ADFS/SSO with regard to both frontend and backend services.
It's using the pac4j library to talk to multiple authentication backends including ADFS over SAML and 
a testing one of a local web form

## How to use it

So long as you have sbt installed, it's pretty simple to get the backend up and running. `sbt run` should do the trick.
All library dependencies are listed in `build.sbt`.

To build the frontend, you need node.js>=12 and npm installed:
```$bash
cd frontend
npm i
npm run dev

```

This will watch the files and rebuild as they are modified.

## How to set it up

You will need to generate a certificate to prove the identity of the service. You can do this
using the JDK's keytool app by running:
```
keytool -genkeypair -alias pac4j-demo -keypass pac4j-demo-passwd -keystore samlKeystore.jks -storepass pac4j-demo-passwd -keyalg RSA -keysize 2048 -validity 3650
```

This creates a file called samlKeystore.jks.  You need to update `conf/application.conf` to add the location of this
file under the existing `saml2` key.  You will also need to add the keystore password (set with `-storepass` above)
and the private key password (set with `-keypass` above).

You then need to export the certificate in base64 format:
```
keytool -exportcert  -keystore samlKeystore.jks -alias pac4j-demo -rfc > pac4j-demo.cer
cat pac4j-demo.cer
```
You should see a standard base64 encoded certificate layout, not an error message.

You also need to deploy the app behind SSL.  This can be annoying during development; the easiest way to handle
it is to configure nginx as a reverse proxy. Say you are running the app on http://localhost:9000, then you configure
nginx to bind to port 443 and serve a vhost at say "adfs-test.local.int".  You add `adfs-test.local.int` to your /etc/hosts
file as 127.0.0.1 and use https://github.com/FiloSottile/mkcert or similar to generate a certificate/key pair that is
locally trusted.

You then set the `baseUrl` parameter in `application.conf` to your 'deployment' location, in this example
 `https://adfs-test.local.int`.


On the ADFS side, you need to configure a "relaying party".
- The relaying party ID is https://deployment-location/callback (details set in app/SecurityModule.scala. 
Basically the string `/callback` is added to the `baseUrl` parameter from the application config)
- You need to add the cert you exported from keytool as the signing certificate.
- The relaying party ID is the whole of the callback URL, typed exactly (in the above example,
 `https://adfs-test.local.int/callback`). It does NOT need to be resolvable from the ADFS host.
 
 Once all those steps are done, you should be able to carry out an authentication to ADFS.

