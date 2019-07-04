/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service.support;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public final class UserInfoFieldsComparator {

  private final static List<String> ORDERED_FIELDS = Lists.newArrayList("username", "email", "firstname", "lastname");

  /**
   * Sort so the important fields appear first
   *
   * @param s1
   * @param s2
   * @return
   */
  public static int compare(String s1, String s2) {
    Integer index1 = ORDERED_FIELDS.indexOf(s1);
    Integer index2 = ORDERED_FIELDS.indexOf(s2);

    if (index1 > -1 && index2 > -1) {
      return index1.compareTo(index2);
    }

    if (index1 < 0) {
      return 1;
    }

    return -1;
  }

  public static int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
    return compare(e1.getKey(), e2.getKey());
  }

}
