package org.obiba.agate.web.model;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.LocalizedString;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
class LocalizedStringDtos {

  Iterable<Agate.LocalizedStringDto> asDto(@SuppressWarnings("TypeMayBeWeakened") LocalizedString localizedString) {
    return localizedString.entrySet().stream().map(
        entry -> Agate.LocalizedStringDto.newBuilder().setLang(entry.getKey().getLanguage()).setValue(entry.getValue())
            .build()
    ).collect(Collectors.toList());
  }

  LocalizedString fromDto(@Nullable Collection<Agate.LocalizedStringDto> dtos) {
    if(dtos == null || dtos.isEmpty()) return null;
    LocalizedString localizedString = new LocalizedString();
    for(Agate.LocalizedStringDto dto : dtos) {
      if(!Strings.isNullOrEmpty(dto.getValue())) {
        localizedString.put(new Locale(dto.getLang()), dto.getValue());
      }
    }
    return localizedString;
  }

  @NotNull
  List<Agate.LocalizedStringDtos> asDtoList(@NotNull Collection<LocalizedString> localizedStrings) {
    return localizedStrings.stream().map(
        localizedString -> Agate.LocalizedStringDtos.newBuilder().addAllLocalizedStrings(asDto(localizedString)).build())
        .collect(Collectors.toList());
  }

  @NotNull
  List<LocalizedString> fromDtoList(@NotNull Collection<Agate.LocalizedStringDtos> dtos) {
    return dtos.stream().map(dto -> fromDto(dto.getLocalizedStringsList())).collect(Collectors.toList());
  }
}
