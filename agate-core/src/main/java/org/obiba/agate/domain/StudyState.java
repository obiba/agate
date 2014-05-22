package org.obiba.agate.domain;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

@Document
public class StudyState extends AbstractAuditableDocument {

  private static final long serialVersionUID = -4271967393906681773L;

  @Indexed
  private RevisionStatus revisionStatus = RevisionStatus.DRAFT;

  @Indexed
  private String publishedTag;

  private int revisionsAhead = 0;

  @NotNull
  private LocalizedString name;

  public String getPublishedTag() {
    return publishedTag;
  }

  public void setPublishedTag(String publishedTag) {
    this.publishedTag = publishedTag;
  }

  public boolean isPublished() {
    return !Strings.isNullOrEmpty(publishedTag);
  }

  public int getRevisionsAhead() {
    return revisionsAhead;
  }

  public void resetRevisionsAhead() {
    revisionsAhead = 0;
  }

  public void incrementRevisionsAhead() {
    revisionsAhead++;
  }

  public boolean hasRevisionsAhead() {
    return revisionsAhead > 0;
  }

  public LocalizedString getName() {
    return name;
  }

  public void setName(LocalizedString name) {
    this.name = name;
  }

  public RevisionStatus getRevisionStatus() {
    return revisionStatus;
  }

  public void setRevisionStatus(RevisionStatus status) {
    revisionStatus = status;
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("name", name) //
        .add("revisionStatus", revisionStatus) //
        .add("publishedTag", publishedTag);
  }

}