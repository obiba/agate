package org.obiba.agate.security;

import eu.flatwhite.shiro.spatial.SingleSpaceRelationProvider;
import eu.flatwhite.shiro.spatial.SingleSpaceResolver;
import eu.flatwhite.shiro.spatial.SpatialPermissionResolver;
import eu.flatwhite.shiro.spatial.finite.NodeRelationProvider;
import eu.flatwhite.shiro.spatial.finite.NodeResolver;

public class AgatePermissionResolver extends SpatialPermissionResolver {

  public AgatePermissionResolver() {
    super(new SingleSpaceResolver(/*new SpatialRealm.RestSpace()*/null), new NodeResolver(),
        new SingleSpaceRelationProvider(new NodeRelationProvider()));
  }

}

