package org.obiba.agate.web.model;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.LocalizedString;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.web.model.translation.JsonTranslator;
import org.obiba.web.model.AuthDtos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDtosTest {

  @InjectMocks
  private UserDtos userDtos;
  @Mock
  private ConfigurationService configurationService;

  @Test
  public void when_convert_user_to_userDto_without_attributes__return_valid_dto() throws Exception {

    // Given
    User user = new User();
    user.setName("userName");
    user.setGroups(new HashSet<>(asList("firstString", "secondString")));

    // Execute
    AuthDtos.SubjectDto subjectDto = userDtos.asDto(user, false);

    // Verify
    assertThat(subjectDto.getUsername(), is("userName"));
    assertThat(subjectDto.getGroupsList(), containsInAnyOrder("secondString", "firstString"));
  }

  @Test
  public void when_convert_user_to_userDto_with_custom_attributes__return_valid_dto_with_only_generic_attributes() throws Exception {

    // Given
    when(configurationService.getConfiguration()).thenReturn(configurationWithAttributes("firstAttribute", "secondAttribute", "thirdAttribute"));
    User user = new User();
    user.setName("username");
    user.setAttribute("firstAttribute", "firstValue");
    user.setAttribute("secondAttribute", "secondValue");
    user.setAttribute("customAttribute", "customValue");

    // Execute
    AuthDtos.SubjectDto userDto = userDtos.asDto(user, true);

    // Verify
    assertThat(userDto.getAttributesList(), hasItems(
      attributeDto("firstAttribute", "firstValue"),
      attributeDto("secondAttribute", "secondValue")));
    assertThat(userDto.getAttributesList(), not(hasItems(attributeDto("customAttribute", "customValue"))));
  }

  @Test
  public void when_convert_user_to_userDto__translate_attributesNames_and_attributesValues() throws Exception {

    // Given
    Configuration configuration = givenConfigurationWithCustomAttributesAndTranslations();
    when(configurationService.getConfiguration()).thenReturn(configuration);
    User user = new User();
    user.setName("User Name");
    user.setFirstName("User First Name");
    user.setAttribute("firstAttribute", "firstValue");
    user.setAttribute("secondAttribute", "enum.secondValue");
    user.setAttribute("thirdAttribute", "false");

    // Execute
    AuthDtos.SubjectDto userDto = userDtos.asDto(user, true, JsonTranslator.getTranslatorFor(JsonPath.parse(configuration.getTranslations().get("en"))));

    // Verify
    assertThat(userDto.getAttributesList(), hasItems(
      attributeDto("First Name", "User First Name"),
      attributeDto("firstAttribute", "firstValue"),
      attributeDto("Second attribute", "Enum second value"),
      attributeDto("thirdAttribute", "False")));
  }

  private Configuration givenConfigurationWithCustomAttributesAndTranslations() {
    Configuration configuration = new Configuration();
    configuration.setUserAttributes(asList(
      new AttributeConfiguration("firstAttribute", AttributeConfiguration.Type.STRING, true, null),
      new AttributeConfiguration("secondAttribute", AttributeConfiguration.Type.STRING, true, asList("enum.firstValue", "enum.secondValue")),
      new AttributeConfiguration("thirdAttribute", AttributeConfiguration.Type.BOOLEAN, true, null)
    ));
    configuration.setTranslations(new LocalizedString(Locale.ENGLISH, "" +
      "{" +
      " \"firstName\":\"First Name\"," +
      " \"secondAttribute\":\"Second attribute\"," +
      " \"false\":\"False\"," +
      " \"enum\":{" +
      "   \"secondValue\":\"Enum second value\"" +
      " }" +
      "}"));
    return configuration;
  }

  private TypeSafeMatcher<AuthDtos.SubjectDto.AttributeDto> attributeDto(String attributeName, String attributeValue) {
    return new TypeSafeMatcher<AuthDtos.SubjectDto.AttributeDto>() {
      @Override
      protected boolean matchesSafely(AuthDtos.SubjectDto.AttributeDto item) {
        return item.getKey().equals(attributeName) && item.getValue().equals(attributeValue);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(String.format("<key: \"%s\", value: \"%s\">", attributeName, attributeValue));
      }
    };
  }

  private Configuration configurationWithAttributes(String... attributeNames) {
    List<AttributeConfiguration> attributes = Arrays.stream(attributeNames)
      .map(attributeName -> new AttributeConfiguration(attributeName, AttributeConfiguration.Type.STRING, false, null))
      .collect(Collectors.toList());

    Configuration configuration = new Configuration();
    configuration.setUserAttributes(attributes);
    return configuration;
  }
}
