package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.StudyState;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class StudySummaryDtos {

  @Inject
  private LocalizedStringDtos localizedStringDtos;

  @NotNull
  Agate.StudySummaryDto asDto(@NotNull StudyState studyState) {
    Agate.StudySummaryDto.Builder builder = Agate.StudySummaryDto.newBuilder();
    builder.setId(studyState.getId()) //
        .setTimestamps(TimestampsDtos.asDto(studyState)) //
        .addAllName(localizedStringDtos.asDto(studyState.getName())) //
        .setRevisionsAhead(studyState.getRevisionsAhead());
    if (studyState.isPublished()) builder.setPublishedTag(studyState.getPublishedTag());
    return builder.build();
  }

}
