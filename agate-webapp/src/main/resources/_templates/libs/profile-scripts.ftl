<script>
  const requiredFields = [
    { name: 'firstname', title: "${messageJs("firstname")}" },
    { name: 'lastname', title: "${messageJs("lastname")}" },
    <#list authConfig.userAttributes as attribute>
    <#if attribute.required>
    { name: '${attribute.name?js_string?no_esc}', title: "${messageJs(attribute.name)}" },
    </#if>
    </#list>
  ];

  const errorMessages = {
      'server.error.password.current-invalid': "${messageJs("server.error.password.current-invalid")}",
      'server.error.password.not-changed': "${messageJs("server.error.password.not-changed")}",
      'server.error.password.too-weak': "${messageJs("server.error.password.too-weak")}"
  };

  agatejs.updateProfile("#profile-form", requiredFields, function (message) {
    var alertId = "#alertProfileFailure";
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

  agatejs.updatePassword("#password-form", function() {
    $("#alertSuccess").removeClass("d-none");
    $("input[type=password]").val("");
    setTimeout(function() {
      $(alertId).addClass("d-none");
    }, 5000);
  }, function (errorKey, responseData) {
    var alertId = "#alert" + errorKey;
    if (errorKey === "Failure" && responseData && responseData.messageTemplate && errorMessages[responseData.messageTemplate]) {
      $(alertId + " > small").text(errorMessages[responseData.messageTemplate]);
    }
    $(alertId).removeClass("d-none");
    setTimeout(function() {
      $(alertId).addClass("d-none");
    }, 5000);
  });

  const enableOtp = function() {
      agatejs.enableOtp((data) => {
          $("#qr-img").attr("src", data);
          $("#qr-panel").show();
          $("#disable-otp").show();
          $("#enable-otp").hide();
          toastr.success("${messageJs("2fa-enable-success")}");
      }, () => {
          toastr.error("${messageJs("2fa-enable-failed")}");
      });
  }

  const disableOtp = () => {
      agatejs.disableOtp(() => {
          $("#qr-panel").hide();
          $("#disable-otp").hide();
          $("#enable-otp").show();
          toastr.success("${messageJs("2fa-disable-success")}");
      }, () => {
          toastr.error("${messageJs("2fa-disable-failed")}");
      });
  }
</script>
