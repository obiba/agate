/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.support;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

public class URLUtils {

  public static Map<String, String> queryStringToMap(String qs) {
    Map<String, String> query = Maps.newHashMap();
    if (!Strings.isNullOrEmpty(qs)) {
      for (String param : Splitter.on("&").split(qs)) {
        String[] tokens = param.split("=");
        if (tokens.length > 1) {
          query.put(decode(tokens[0]), decode(tokens[1]));
        }
      }
    }
    return query;
  }

  public static String decode(String text) {
    try {
      return URLDecoder.decode(text, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return text;
    }
  }

  public static String encode(String text) {
    try {
      return URLEncoder.encode(text, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return text;
    }
  }

}
