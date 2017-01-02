/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

