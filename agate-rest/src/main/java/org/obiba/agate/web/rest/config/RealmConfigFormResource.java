package org.obiba.agate.web.rest.config;

import com.google.common.collect.Maps;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.service.support.LdapRealmConfigForm;
import org.obiba.agate.service.support.RealmConfigForm;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Map;

@Component
@Path("/config/realm-form")
public class RealmConfigFormResource {

  @GET
  public Map<String, Object> getForm() {
    Map<String, Object> form = Maps.newHashMap();
    form.put("form", RealmConfigForm.getForm().toString());
    form.put(AgateRealm.AGATE_LDAP_REALM.toString(), LdapRealmConfigForm.getForm().toString());
    return form;
  }
}
