<!-- REQUIRED SCRIPTS -->

<!-- jQuery -->
<script src="${jqueryPath}/jquery.min.js"></script>
<!-- Bootstrap 4 -->
<script src="${bootstrapPath}/js/bootstrap.bundle.min.js"></script>
<!-- AdminLTE App -->
<script src="${adminLTEPath}/dist/js/adminlte.min.js"></script>
<!-- DataTables -->
<#--<script src="${adminLTEPath}/plugins/datatables/jquery.dataTables.js"></script>-->
<#--<script src="${adminLTEPath}/plugins/datatables-bs4/js/dataTables.bootstrap4.js"></script>-->
<!-- Toastr -->
<script src="${toastrPath}/toastr.min.js"></script>
<!-- Axios -->
<script src="${assetsPath}/libs/node_modules/axios/dist/axios.min.js"></script>

<!-- Agate Utils and dependencies -->
<script src="${assetsPath}/libs/node_modules/jquery.redirect/jquery.redirect.js"></script>
<script src="${assetsPath}/js/agate.js"></script>

<!-- Global js variables -->
<script>
  const contextPath = "${contextPath}";
</script>

<!-- Custom js -->
<#include "../models/scripts.ftl"/>
