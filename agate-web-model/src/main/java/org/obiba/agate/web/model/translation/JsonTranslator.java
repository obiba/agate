/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model.translation;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;

public class JsonTranslator implements Translator {

  private DocumentContext documentContext;

  public JsonTranslator(DocumentContext documentContext) {
    this.documentContext = documentContext;
  }

  @Override
  public String translate(String toTranslate) {
    try {
      return documentContext.read(toTranslate, String.class);
    } catch (PathNotFoundException e) {
      return toTranslate;
    }
  }

  public static Translator getTranslatorFor(DocumentContext documentContext) {
    return new JsonTranslator(documentContext);
  }
}
