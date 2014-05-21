package org.obiba.agate.web.rest.study;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.agate.domain.Study;
import org.obiba.agate.service.StudyService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing draft Study.
 */
@Component
@Scope("request")
public class DraftStudyResource {

  @Inject
  private StudyService studyService;

  @Inject
  private Dtos dtos;

  private String id;

  public void setId(String id) {
    this.id = id;
  }

  @GET
  @Timed
  public Agate.StudyDto get() {
    return dtos.asDto(studyService.findDraftStudy(id));
  }

  @PUT
  @Timed
  public Response update(@SuppressWarnings("TypeMayBeWeakened") Agate.StudyDto studyDto) {
    // ensure study exists
    studyService.findDraftStudy(id);

    Study study = dtos.fromDto(studyDto);
    studyService.save(study);
    return Response.noContent().build();
  }

  @PUT
  @Path("/_publish")
  public Response publish() {
    studyService.publish(id);
    return Response.noContent().build();
  }

  /**
   * DELETE  /ws/studies/:id -> delete the "id" study.
   */
//  @DELETE
//  @Timed
//  public Response delete() {
//    studyService.delete(id);
//    return Response.noContent().build();
//  }

}
