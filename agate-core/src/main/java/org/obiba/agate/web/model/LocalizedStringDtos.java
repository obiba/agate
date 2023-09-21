/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import com.google.common.base.Strings;
import org.obiba.agate.domain.LocalizedString;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class LocalizedStringDtos {

  Iterable<Agate.LocalizedStringDto> asDto(@SuppressWarnings("TypeMayBeWeakened") LocalizedString localizedString) {
    if (localizedString == null) return Collections.emptyList();
    return localizedString.entrySet().stream().map(
        entry -> Agate.LocalizedStringDto.newBuilder().setLang(entry.getKey()).setValue(entry.getValue())
            .build()
    ).collect(Collectors.toList());
  }

  LocalizedString fromDto(@Nullable Collection<Agate.LocalizedStringDto> dtos) {
    if(dtos == null || dtos.isEmpty()) return null;
    LocalizedString localizedString = new LocalizedString();
    for(Agate.LocalizedStringDto dto : dtos) {
      if(!Strings.isNullOrEmpty(dto.getValue())) {
        localizedString.put(dto.getLang(), dto.getValue());
      }
    }
    return localizedString;
  }

  public Iterable<Agate.LocalizedStringDto> asDto(Map<String, String> localizedMap) {
    return asDto(localizedMap, null);
  }

  /**
   *
   * @param localizedMap
   * @param locale Preferred locale if exists, otherwise all locales are returned
   * @return
   */
  public Iterable<Agate.LocalizedStringDto> asDto(Map<String, String> localizedMap, @Nullable String locale) {
    if (localizedMap == null || localizedMap.isEmpty()) return Collections.emptyList();
    if (localizedMap.containsKey(locale)) {
      return Collections.singleton(Agate.LocalizedStringDto.newBuilder().setLang(locale).setValue(localizedMap.get(locale))
        .build());
    }
    return localizedMap.entrySet().stream().map(
        entry -> Agate.LocalizedStringDto.newBuilder().setLang(entry.getKey()).setValue(entry.getValue())
            .build()
    ).collect(Collectors.toList());
  }
}
