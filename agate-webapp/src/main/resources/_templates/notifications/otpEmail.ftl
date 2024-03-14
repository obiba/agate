<!DOCTYPE html>
<head lang="en">
  <meta charset="UTF-8"/>
  <title></title>
</head>
<body style="margin: 0;font-family:'Nunito',arial,sans-serif;line-height:1.5;font-size:18px;color: #2c3e50;background-color: #ffffff;">

<div style="min-height: 20px;padding: 30px;margin-bottom: 20px;background-color: #ecf0f1;border-radius: 10px;-webkit-box-shadow: none;box-shadow: none;">
  <div style="margin: auto;max-width: 600px;">
    <div style="margin-bottom: 21px;background-color: #ffffff;border-radius: 10px;-webkit-box-shadow: 0 1px 1px rgba(0,0,0,0.05);box-shadow: 0 1px 1px rgba(0,0,0,0.05);">
      <div style="padding: 20px 30px;border-top-right-radius: 10px;border-top-left-radius: 10px;background-color: gainsboro;">
        <div style="font-size:22px !important;font-weight: bold;color: inherit;margin-top: 0;margin-bottom: 0;font-size: 17px;">
          ${msg("email.otp.title", organization)}
        </div>
      </div>
      <div style="padding: 30px;">
        <p style="margin: 0 0 30px;">
          ${msg("email.generic.presentation", user.firstName!"", user.lastName!"")}
        </p>
        <p style="margin: 0 0 30px;">
          ${msg("email.otp.body", timeout)}
        </p>
        <p style="margin: 0 0 30px;">
        </p>
        <p style="margin: auto;text-align: center;">
          <span style="font-size:22px !important;font-weight: bold;color:#1f2d3d;font-weight:bold;background-color:#ffc107;padding:10px 20px;border-radius:15px;">
            ${code}
          </span>
        </p>
      </div>
    </div>
    <p style="display: block;margin: 5px 0 10px;font-size:14px;line-height:2;color:#aaa;">
      ${msg("email.generic.message")}
    </p>
  </div>
</div>

</body>
</html>
