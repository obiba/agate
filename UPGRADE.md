# Upgrade notes

Actions to perform when upgrading an Agate server. Go through every version between the
one installed and the one being deployed.

## 5.0.0

### Before upgrading

1. Back up the MongoDB database. A rollback to 4.2 requires restoring it: 4.2 cannot read
   the password hashes written by 4.3.
2. In `AGATE_HOME/conf/application-prod.yml`, note the current values of
   `shiro.password.salt` and `shiro.password.nbHashIterations`. **Do not remove them**
   during the upgrade: they are still needed to verify passwords and application keys that
   have not been re-hashed yet.
3. Check for overridden templates:

   ```sh
   ls -R $AGATE_HOME/conf/templates
   ```

   If the directory exists and is not empty, keep a copy of the current bundled templates
   (`$AGATE_DIST/WEB-INF/classes/_templates`) to compare against.

### Upgrade

Install the new version and restart as usual. No database migration runs at startup.

### After upgrading

1. **Overridden templates** (skip if `AGATE_HOME/conf/templates` is empty).

   Every `${…}` in a template is now HTML-escaped, in web pages and notification e-mails.

   1. If `libs/head.ftl` is overridden, replace it with the 4.3 version and re-apply your
      customisations. This one is required.
   2. For each of `signin.ftl`, `signup.ftl`, `signup-with.ftl`, `signout.ftl`,
      `libs/scripts.ftl`, `libs/signin-scripts.ftl`, `libs/signout-scripts.ftl`,
      `libs/profile-scripts.ftl`, `libs/navbar-menus-right.ftl` that is overridden: start
      from the 4.3 version and re-apply your customisations. For `signin.ftl` and
      `libs/signin-scripts.ftl` this also brings the invalid-code message on the 2FA step
      (see point 6); without it the page keeps working but shows the generic
      authentication failure instead.
   3. In every other overridden template, find the interpolations written inside scripts:

      ```sh
      cd $AGATE_HOME/conf/templates
      awk '/<script/{s=1} /<\/script>/{s=0} s && /\$\{/ {print FILENAME":"FNR": "$0}' $(find . -name '*.ftl')
      grep -rn 'on[a-z]*="[^"]*\${' --include='*.ftl' .
      ```

      Append `?js_string?no_esc` to those inside a `<script>` block, and `?js_string` to
      those inside an `on…="…"` attribute.
   4. If a template outputs a variable that contains HTML on purpose, append `?no_esc` to
      it — only for values that cannot come from a user or an identity provider. If a
      template applied `?html` itself, remove it (it now double-escapes).
   5. Open sign-in, sign-up (including sign-up through an external identity provider),
      sign-out with a `post_logout_redirect_uri`, the profile page, and trigger one
      notification e-mail. A visible `&amp;` or `&lt;` points to a template still to fix.

2. **Registered applications**: in the administration UI, open each application and review
   its redirect URIs. A request redirect is now accepted only if scheme, host and port are
   identical to a registered URI and its path is the registered path or a sub-path of it.
   Add an entry (comma separated) for every host, port or scheme actually used by the
   application. A base URL such as `https://app.example.org` still covers every path on
   that host.

3. **OAuth2 clients**: authorization codes now expire after 300 seconds and can be
   exchanged only once. Standard clients need nothing. If a custom client needs longer,
   set `oauth.codeTimeout` (seconds) in `application-prod.yml`. Users who were in the
   middle of an authorization during the restart sign in again.

4. **Scripts calling `POST /ws/users/_test`**: this endpoint now requires an
   `agate-administrator` session. Update any monitoring or provisioning script that used
   it anonymously.

5. Delete `AGATE_HOME/conf/ESAPI.properties` if such a file exists; it is no longer read.

6. **Two-factor authentication.** The password is now verified before the 2FA code is
   asked for (it was the other way around). Consequences to be aware of:

   1. A wrong password is refused right away from the credentials form and counts toward
      the login ban (`login.maxTry`, `login.trialTime`, `login.banTime` in
      `application-prod.yml`); it used to be reported as a 2FA failure and never counted.
      Expect a few more bans in the first days from users who had learnt to retry.
   2. A wrong code keeps the 2FA step open and shows a dedicated message
      (`sign-in-otp-failed`, bundled in English and French; add it to any other language
      you provide).
   3. If 2FA is enforced, users of external realms (LDAP, Active Directory, SQL) who never
      finished enrolling an authenticator were let in without one. They are now asked to
      scan the QR code and enter a code at their next sign-in. Warn them if you have such
      users.
   4. Applications that delegate authentication to Agate get the fix without any change.
      Upgrade Agate before Mica 6.4.0 and Opal (see their own upgrade notes): their
      sign-in pages assume the new order when labelling a failure on the 2FA step as a
      wrong code.
   5. Check with a test user for whom 2FA is enforced or activated: a wrong password is
      refused from the credentials form without any QR code being shown; a wrong code
      keeps the 2FA step open with the invalid-code message.

### In the following weeks

Passwords and application keys are re-hashed with Argon2id on the next successful
authentication of each user and application. To see what is left:

```js
db.userCredentials.countDocuments({ password: { $not: /^\$shiro2\$/ } })
db.application.countDocuments({ key: { $not: /^\$shiro2\$/ } })
```

When both return `0`, remove the `shiro.password` block from `application-prod.yml` and
restart. Until then, leave it exactly as it was.

### Rolling back

Stop the server, restore the database backup taken before the upgrade, and reinstall 4.2.
Do not roll back on the current database: every user and application that authenticated
since the upgrade has an Argon2 hash that 4.2 cannot verify.
