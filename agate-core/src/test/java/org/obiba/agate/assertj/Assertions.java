package org.obiba.agate.assertj;

import org.obiba.agate.domain.Study;

public class Assertions extends org.assertj.core.api.Assertions {

  public static StudyAssert assertThat(Study actual) {
    return new StudyAssert(actual);
  }

}
