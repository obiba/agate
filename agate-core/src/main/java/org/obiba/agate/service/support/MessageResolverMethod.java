/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service.support;

import com.google.common.base.Strings;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

public class MessageResolverMethod implements TemplateMethodModel {

  private MessageSource messageSource;
  private Locale locale;

  public MessageResolverMethod(MessageSource messageSource, Locale locale) {
    this.messageSource = messageSource;
    this.locale = locale;
  }

  @Override
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.isEmpty()) {
      throw new TemplateModelException("Message code is missing");
    }
    String code = (String) arguments.get(0);
    if (Strings.isNullOrEmpty(code)) {
      throw new TemplateModelException("Invalid code value '" + code + "'");
    }
    Object[] args = null;
    if (arguments.size()>1) {
      args = new Object[arguments.size() - 1];
      for (int i = 1; i<arguments.size(); i++) {
        args[i - 1] = arguments.get(i);
      }
    }
    return messageSource.getMessage(code, args, locale);
  }
}
