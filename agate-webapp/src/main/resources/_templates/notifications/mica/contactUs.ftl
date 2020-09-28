<!DOCTYPE html>
<head lang="en">
  <meta charset="UTF-8"/>
  <title></title>
</head>
<body
  style="margin: 0;font-family: 'Lato','Helvetica Neue',Helvetica,Arial,sans-serif;font-size: 15px;line-height: 1.5;color: #2c3e50;background-color: #ffffff;">

<div
  style="min-height: 20px;padding: 19px;margin-bottom: 20px;background-color: #ecf0f1;border: 1px solid transparent;border-radius: 4px;-webkit-box-shadow: none;box-shadow: none;">
  <div style="margin: auto;max-width: 700px;">
    <div
      style="margin-bottom: 21px;background-color: #ffffff;border-radius: 4px;-webkit-box-shadow: 0 1px 1px rgba(0,0,0,0.05);box-shadow: 0 1px 1px rgba(0,0,0,0.05);border: 1px solid gainsboro;">
      <div
        style="padding: 10px 15px;border-top-right-radius: 3px;border-top-left-radius: 3px;background-color: gainsboro;border-color: gainsboro;">
        <h3 style="font-family: 'Lato','Helvetica Neue',Helvetica,Arial,sans-serif;font-weight: 400;line-height: 1.1;color: inherit;margin-top: 0;margin-bottom: 0;font-size: 17px;">
          ${msg("mica.email.contactUs.title", organization)}
        </h3>
      </div>
      <div style="padding: 15px;">
        <p style="margin: 0 0 10px;">
          ${msg("email.generic.presentation", user.firstName!"", user.lastName!"")}
        </p>
        <p style="margin: 0 0 10px;">
          ${msg("mica.email.contactUs.text")}
        </p>
        <p style="margin: 0 0 10px;">
          <dl>
            <dt>${msg("mica.email.contactUs.name")}</dt>
            <dd>${contactName}</dd>
            <dt>${msg("mica.email.contactUs.email")}</dt>
            <dd><a href="mailto:${contactEmail}">${contactEmail}</a></dd>
            <dt>${msg("mica.email.contactUs.subject")}</dt>
            <dd>${contactSubject}</dd>
            <dt>${msg("mica.email.contactUs.message")}</dt>
            <dd>
              <pre>${contactMessage}</pre>
            </dd>
          </dl>
        </p>
      </div>
    </div>
    <p style="display: block;margin: 5px 0 10px;color: #597ea2;">
      ${msg("email.generic.message")}
    </p>
  </div>
</div>

</body>
</html>
