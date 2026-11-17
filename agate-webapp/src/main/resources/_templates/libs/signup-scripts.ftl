<script>
  const errorMessages = {
    'server.error.bad-request': "${messageJs("server.error.bad-request")}",
    'server.error.bad-captcha': "${messageJs("server.error.bad-captcha")}",
    'server.error.email-already-assigned': "${messageJs("server.error.email-already-assigned")}",
    'error.unhandledException': "${messageJs("server.error.unhandledException")}"
  };
  agatejs.signup("form", requiredFields, function (message) {
    var alertId = "#alertFailure";
    var msg = message;
    if (Array.isArray(message)) {
      msg = "${messageJs("sign-up-fields-required")}: " + message.join(", ");
    } else if (errorMessages[msg]) {
      msg = errorMessages[msg];
    }
    $(alertId).html('<small>' + msg + '</small>').removeClass("d-none");
    setTimeout(function() {
      $(alertId).addClass("d-none");
    }, 5000);
  });
</script>
