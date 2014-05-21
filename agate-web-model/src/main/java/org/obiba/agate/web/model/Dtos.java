package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.AgateConfig;
import org.obiba.agate.domain.Network;
import org.obiba.agate.domain.Study;
import org.obiba.agate.domain.StudyState;
import org.obiba.agate.service.StudyService;
import org.springframework.stereotype.Component;

import static org.obiba.agate.web.model.Agate.AgateConfigDto;
import static org.obiba.agate.web.model.Agate.AgateConfigDtoOrBuilder;
import static org.obiba.agate.web.model.Agate.NetworkDto;
import static org.obiba.agate.web.model.Agate.NetworkDtoOrBuilder;
import static org.obiba.agate.web.model.Agate.StudyDto;
import static org.obiba.agate.web.model.Agate.StudyDtoOrBuilder;
import static org.obiba.agate.web.model.Agate.StudySummaryDto;

@Component
@SuppressWarnings("OverlyCoupledClass")
public class Dtos {

  @Inject
  private StudyService studyService;

  @Inject
  private StudyDtos studyDtos;

  @Inject
  private AgateConfigDtos agateConfigDtos;

  @Inject
  private NetworkDtos networkDtos;

  @Inject
  private StudySummaryDtos studySummaryDtos;

  @NotNull
  public StudyDto asDto(@NotNull Study study) {
    return studyDtos.asDto(study, studyService.findStateByStudy(study));
  }

  @NotNull
  public StudySummaryDto asDto(@NotNull StudyState studyState) {
    return studySummaryDtos.asDto(studyState);
  }

  @NotNull
  public Study fromDto(@NotNull StudyDtoOrBuilder dto) {
    return studyDtos.fromDto(dto);
  }

  @NotNull
  public NetworkDto asDto(@NotNull Network network) {
    return networkDtos.asDto(network);
  }

  @NotNull
  public Network fromDto(@NotNull NetworkDtoOrBuilder dto) {
    return networkDtos.fromDto(dto);
  }

  @NotNull
  public AgateConfigDto asDto(@NotNull AgateConfig agateConfig) {
    return agateConfigDtos.asDto(agateConfig);
  }

  @NotNull
  public AgateConfig fromDto(@NotNull AgateConfigDtoOrBuilder dto) {
    return agateConfigDtos.fromDto(dto);
  }

}
