package org.obiba.agate.web.rest.study;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.agate.domain.Study;
import org.obiba.agate.service.StudyService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.context.ApplicationContext;

import com.codahale.metrics.annotation.Timed;

@Path("/draft")
public class DraftStudiesResource {

  @Inject
  private StudyService studyService;

  @Inject
  private Dtos dtos;

  @Inject
  private ApplicationContext applicationContext;

  @GET
  @Path("/studies")
  @Timed
  public List<Agate.StudyDto> list() {
    return studyService.findAllDraftStudies().stream().map(dtos::asDto).collect(Collectors.toList());
  }

  @POST
  @Path("/studies")
  @Timed
  public Response create(@SuppressWarnings("TypeMayBeWeakened") Agate.StudyDto studyDto, @Context UriInfo uriInfo) {
    Study study = dtos.fromDto(studyDto);
    studyService.save(study);
    return Response.created(uriInfo.getBaseUriBuilder().path(DraftStudiesResource.class, "study").build(study.getId()))
        .build();
  }

  @Path("/study/{id}")
  public DraftStudyResource study(@PathParam("id") String id) {
    DraftStudyResource studyResource = applicationContext.getBean(DraftStudyResource.class);
    studyResource.setId(id);
    return studyResource;
  }

}