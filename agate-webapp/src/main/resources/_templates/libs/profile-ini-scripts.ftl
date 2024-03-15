<script>

  const enableOtp = function() {
      agatejs.enableConfigOtp((data) => {
          console.log(data);
          $("#qr-img").attr("src", data);
          $("#qr-panel").show();
          $("#disable-otp").show();
          $("#enable-otp").hide();
          toastr.success("<@message "2fa-enable-success"/>");
      }, () => {
          toastr.error("<@message "2fa-enable-failed"/>");
      });
  }

  const disableOtp = () => {
      agatejs.disableConfigOtp(() => {
          $("#qr-panel").hide();
          $("#disable-otp").hide();
          $("#enable-otp").show();
          toastr.success("<@message "2fa-disable-success"/>");
      }, () => {
          toastr.error("<@message "2fa-disable-failed"/>");
      });
  }
</script>
