<!DOCTYPE html>
<head lang="en">
  <meta charset="UTF-8"/>
  <title></title>
</head>
<body style="margin: 0;font-family: 'Lato','Helvetica Neue',Helvetica,Arial,sans-serif;font-size: 15px;line-height: 1.5;color: #2c3e50;background-color: #ffffff;">

<div style="min-height: 20px;padding: 19px;margin-bottom: 20px;background-color: #ecf0f1;border: 1px solid transparent;border-radius: 4px;-webkit-box-shadow: none;box-shadow: none;">
  <div style="margin: auto;max-width: 700px;">
    <div style="margin-bottom: 21px;background-color: #ffffff;border-radius: 4px;-webkit-box-shadow: 0 1px 1px rgba(0,0,0,0.05);box-shadow: 0 1px 1px rgba(0,0,0,0.05);border: 1px solid gainsboro;">
      <div style="padding: 10px 15px;border-top-right-radius: 3px;border-top-left-radius: 3px;background-color: gainsboro;border-color: gainsboro;">
        <h3 style="font-family: 'Lato','Helvetica Neue',Helvetica,Arial,sans-serif;font-weight: 400;line-height: 1.1;color: inherit;margin-top: 0;margin-bottom: 0;font-size: 17px;">
          ${msg("mica.email.dataAccessRequestFeasibilityApprovedApplicant.title", organization)}
        </h3>
      </div>
      <div style="padding: 15px;">
        <p style="margin: 0 0 10px;">
          ${msg("email.generic.presentation", user.firstName!"", user.lastName!"")}
        </p>
        <p style="margin: 0 0 10px;">
          ${msg("mica.email.dataAccessRequestFeasibilityApprovedApplicant.body", title)}
        </p>
        <p style="margin: 0 0 10px;">
          ${msg("mica.email.dataAccessRequestFeasibilityApprovedApplicant.details")}
        </p>
        <p style="margin: 0 0 10px;">
        </p><p style="margin: auto;text-align: center;">
          <a href="${publicUrl}/data-access-feasibility-form/${id}" target="_blank"
             style="color: #ffffff;text-decoration: none;display: inline-block;margin-bottom: 0;font-weight: normal;text-align: center;vertical-align: middle;-ms-touch-action: manipulation;touch-action: manipulation;cursor: pointer;background: #2c3e50 none;white-space: nowrap;padding: 10px 15px;font-size: 15px;line-height: 1.5;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;user-select: none;border: 1px solid #2c3e50;">
            ${msg("mica.email.dataAccessRequestFeasibilityApprovedApplicant.link")}
          </a>
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

