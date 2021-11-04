<script>
  const requiredFields = [
    { name: 'firstname', title: "<@message "firstname"/>" },
    { name: 'lastname', title: "<@message "lastname"/>" },
    <#list authConfig.userAttributes as attribute>
    <#if attribute.required>
    { name: '${attribute.name}', title: "<@message attribute.name/>" },
    </#if>
    </#list>
  ];

  const errorMessages = {
      'server.error.password.not-changed': "<@message "server.error.password.not-changed"/>",
      'server.error.password.too-weak': "<@message "server.error.password.too-weak"/>"
  };

  agatejs.updateProfile("#profile-form", requiredFields, function (message) {
    var alertId = "#alertProfileFailure";
    var msg = message;
    if (Array.isArray(message)) {
      msg = "<@message "sign-up-fields-required"/>: " + message.join(", ");
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
</script>
