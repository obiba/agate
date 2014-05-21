package org.obiba.agate.web.model;

import java.util.Locale;

import javax.inject.Inject;

import org.obiba.agate.domain.Address;
import org.obiba.agate.domain.Contact;
import org.obiba.agate.service.AgateConfigService;
import org.springframework.stereotype.Component;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component
@SuppressWarnings("OverlyCoupledClass")
class ContactDtos {

  @Inject
  private AgateConfigService agateConfigService;

  @Inject
  private LocalizedStringDtos localizedStringDtos;

  Agate.ContactDto asDto(Contact contact) {
    Agate.ContactDto.Builder builder = Agate.ContactDto.newBuilder().setLastName(contact.getLastName());
    if(!isNullOrEmpty(contact.getTitle())) builder.setTitle(contact.getTitle());
    if(!isNullOrEmpty(contact.getFirstName())) builder.setFirstName(contact.getFirstName());
    if(!isNullOrEmpty(contact.getEmail())) builder.setEmail(contact.getEmail());
    if(!isNullOrEmpty(contact.getPhone())) builder.setPhone(contact.getPhone());
    if(contact.getInstitution() != null) builder.setInstitution(asDto(contact.getInstitution()));
    return builder.build();
  }

  Contact fromDto(Agate.ContactDtoOrBuilder dto) {
    Contact contact = new Contact();
    if(dto.hasTitle()) contact.setTitle(dto.getTitle());
    if(dto.hasFirstName()) contact.setFirstName(dto.getFirstName());
    contact.setLastName(dto.getLastName());
    if(dto.hasEmail()) contact.setEmail(dto.getEmail());
    if(dto.hasPhone()) contact.setPhone(dto.getPhone());
    if(dto.hasInstitution()) contact.setInstitution(fromDto(dto.getInstitution()));
    return contact;
  }

  private Agate.ContactDto.InstitutionDto asDto(Contact.Institution institution) {
    Agate.ContactDto.InstitutionDto.Builder builder = Agate.ContactDto.InstitutionDto.newBuilder();
    if(institution.getName() != null) builder.addAllName(localizedStringDtos.asDto(institution.getName()));
    if(institution.getDepartment() != null) {
      builder.addAllDepartment(localizedStringDtos.asDto(institution.getDepartment()));
    }
    if(institution.getAddress() != null) builder.setAddress(asDto(institution.getAddress()));
    return builder.build();
  }

  private Contact.Institution fromDto(Agate.ContactDto.InstitutionDtoOrBuilder dto) {
    Contact.Institution institution = new Contact.Institution();
    institution.setName(localizedStringDtos.fromDto(dto.getNameList()));
    institution.setDepartment(localizedStringDtos.fromDto(dto.getDepartmentList()));
    if(dto.hasAddress()) institution.setAddress(fromDto(dto.getAddress()));
    return institution;
  }

  private Agate.AddressDto asDto(Address address) {
    Agate.AddressDto.Builder builder = Agate.AddressDto.newBuilder();
    if(address.getStreet() != null) builder.addAllStreet(localizedStringDtos.asDto(address.getStreet()));
    if(address.getCity() != null) builder.addAllCity(localizedStringDtos.asDto(address.getCity()));
    if(!isNullOrEmpty(address.getZip())) builder.setZip(address.getZip());
    if(!isNullOrEmpty(address.getState())) builder.setState(address.getState());
    if(!isNullOrEmpty(address.getCountryIso())) builder.setCountry(asDto(address.getCountryIso()));
    return builder.build();
  }

  private Address fromDto(Agate.AddressDtoOrBuilder dto) {
    Address address = new Address();
    address.setStreet(localizedStringDtos.fromDto(dto.getStreetList()));
    address.setCity(localizedStringDtos.fromDto(dto.getCityList()));
    if(dto.hasZip()) address.setZip(dto.getZip());
    if(dto.hasState()) address.setState(dto.getState());
    if(dto.hasCountry()) address.setCountryIso(dto.getCountry().getIso());
    return address;
  }

  private Agate.CountryDto asDto(String countryIso) {
    Agate.CountryDto.Builder builder = Agate.CountryDto.newBuilder().setIso(countryIso);
    agateConfigService.getConfig().getLocales().forEach(locale -> builder.addName(
        Agate.LocalizedStringDto.newBuilder().setLang(locale.getLanguage())
            .setValue(new Locale(countryIso).getDisplayCountry(locale))
    ));
    return builder.build();
  }

}
