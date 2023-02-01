<script>
    agatejs.confirmAndSetPassword("#form", function (errorKey) {
        var alertId = "#alert" + errorKey;
        $(alertId).removeClass("d-none");
        setTimeout(function() {
            $(alertId).addClass("d-none");
        }, 5000);
    });
</script>