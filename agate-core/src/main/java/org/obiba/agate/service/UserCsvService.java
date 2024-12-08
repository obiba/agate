package org.obiba.agate.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.joda.time.DateTime;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UserCsvService {
  private ConfigurationService configurationService;

  @Inject
  public UserCsvService(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  public ByteArrayOutputStream toCsv(List<User> users) throws IOException {

    ByteArrayOutputStream csv = new ByteArrayOutputStream();
    Writer writer = null;
    try {
      writer = new OutputStreamWriter(csv, "UTF-8");
      List<String> lines = getLines(users);

      for (String line : lines) {
        writer.write(line);
        writer.write("\n");
      }
    } finally {
      if (writer != null) writer.close();
    }

    return csv;
  }

  private List<String> getLines(List<User> users) {

    final List<String> header = getHeaderFromConfiguration();

    List<String> initList = new ArrayList<String>() {{
      add(convertToLine(header));
    }};

    return Stream.concat(
      initList.stream(),
      users.stream()
      .map(user -> this.extractFieldsFromUser(user, header))
      .map(this::convertToLine))
    .collect(Collectors.toList());
  }

  private List<String> getHeaderFromConfiguration() {
    Configuration configuration = this.configurationService.getConfiguration();

    List<String> headerNameList = new ArrayList<String>() {{
      add("username");
      add("firstName");
      add("lastName");
      add("email");
      add("preferredLanguage");
      add("status");
      add("lastLogin");
    }};

    return Stream.concat(
      headerNameList.stream(),
      configuration.getUserAttributes().stream().map(attr -> "attribute." + attr.getName()))
    .collect(Collectors.toList());
  }

  private List<String> extractFieldsFromUser(User user, List<String> headerFromConfiguration) {
    List<String> data = new ArrayList<String>() {{
      add(user.getName());
      add(user.getFirstName());
      add(user.getLastName());
      add(user.getEmail());
      add(user.getPreferredLanguage());
      add(user.getStatus().toString().toUpperCase());
      add(Optional.ofNullable(user.getLastLogin()).map(DateTime::toString).orElse(""));
    }};

    return Stream.concat(
      data.stream(),
      headerFromConfiguration.stream()
        .filter(header -> header.startsWith("attribute."))
        .map(header -> header.replace("attribute.", ""))
        .map(header -> user.getAttributes().getOrDefault(header, ""))
        .map(this::escapeSpecialCharacters))
    .collect(Collectors.toList());
  }

  private String escapeSpecialCharacters(String data) {
    String escapedData = data.replaceAll("\\R", " ");
    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
      data = data.replace("\"", "\"\"");
      escapedData = "\"" + data + "\"";
    }
    return escapedData;
  }

  private String convertToLine(List<String> line) {
    return line.stream().collect(Collectors.joining(","));
  }
}
