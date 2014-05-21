package org.obiba.agate.web.rest.study;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.agate.service.StudyService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.context.ApplicationContext;

import com.codahale.metrics.annotation.Timed;

@Path("/draft")
public class DraftStudySummariesResource {

  @Inject
  private StudyService studyService;

  @Inject
  private Dtos dtos;

  @Inject
  private ApplicationContext applicationContext;

  @GET
  @Path("/study-summaries")
  @Timed
  public List<Agate.StudySummaryDto> list() {
    return studyService.findAllStates().stream().map(dtos::asDto).collect(Collectors.toList());
  }
  @Path("/study-summary/{id}")
  public DraftStudySummaryResource study(@PathParam("id") String id) {
    DraftStudySummaryResource studyStateResource = applicationContext.getBean(DraftStudySummaryResource.class);
    studyStateResource.setId(id);
    return studyStateResource;
  }
}
