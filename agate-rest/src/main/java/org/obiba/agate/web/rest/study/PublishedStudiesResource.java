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

@Path("/")
public class PublishedStudiesResource {

  @Inject
  private StudyService studyService;

  @Inject
  private Dtos dtos;

  @Inject
  private ApplicationContext applicationContext;

  @GET
  @Path("/studies")
  @Timed
  public List<Agate.StudySummaryDto> list() {
    return studyService.findPublishedStates().stream().map(dtos::asDto).collect(Collectors.toList());
  }

  @Path("/study/{id}")
  public PublishedStudyResource study(@PathParam("id") String id) {
    PublishedStudyResource studyResource = applicationContext.getBean(PublishedStudyResource.class);
    studyResource.setId(id);
    return studyResource;
  }

}
