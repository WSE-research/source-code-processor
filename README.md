# Source code retrieval Annotation processor

## Goal

This annotation processor aims to parse source code at compile-time to provide it during runtime.

## Usage

### Implementation

To use this annotation processor for your application, add the dependency as well as the maven-compiler plugin with the annotationProcessor as annotationProcessorPath.

**Dependency:**

```xml
<dependency>
  <groupId>org.wseresearch</groupId>
  <artifactId>source-code-processor</artifactId>
  <version>0.0.1</version>
</dependency>
```

**Maven-compiler Plugin configuration:**

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.11.0</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>org.wseresearch</groupId>
        <artifactId>source-code-processor</artifactId>
        <version>0.0.1</version>
      </path>
    </annotationProcessorPaths>
    <generatedSourcesDirectory>${project.build.directory}/generated-sources/annotations</generatedSourcesDirectory>
  </configuration>
</plugin>
```

### In-Application Usage
When included compiled in target app, the created json-files are accessible in the Classpath + "json" directory.
The files' names are the package names + class name. 

To access the files during runtime, you must read the file of the requested class and then you can access the JSON
with a library of your choise, e.g. Jackson. The JSON's schema is as follows:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Methods",
  "type": "object",
  "properties": {
    "methods": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "fqn": {
            "type": "string"
          },
          "methodName": {
            "type": "string"
          },
          "returnType": {
            "type": "string"
          },
          "sourceCode": {
            "type": "string"
          },
          "parameterTypes": {
            "type": "array",
            "items": {
              "type": "string"
            }
          }
        },
        "required": [
          "fqn",
          "methodName",
          "returnType",
          "sourceCode",
          "parameterTypes"
        ],
        "additionalProperties": false
      }
    }
  },
  "required": ["methods"],
  "additionalProperties": false
}
```

### Exemplary property access

Given the following JSON (`eu.wdaqua.qanary.web.QanaryWebConfiguration.json`):
```json
{
  "methods": [
    {
      "fqn": "eu.wdaqua.qanary.web.QanaryWebConfiguration",
      "methodName": "addViewControllers",
      "returnType": "void",
      "sourceCode": "public void addViewControllers(ViewControllerRegistry registry) {      registry.addViewController(\"/static\").setViewName(\"static\");  }",
      "parameterTypes": [
        "org.springframework.web.servlet.config.annotation.ViewControllerRegistry"
      ]
    }
  ]
}
```
#### Via JsonMethodFileReader

With this Processor, we also provide a FileReader (JsonMethodFileReader) that returns a MethodInfo object 
for a requested method. With `JsonMethodFileReader.getMethod(fqn, methodName, params)` the requested method
will be searched for.

#### Own implementation

If you rather implement the retrieval function yourself, you can take the following snippet as a starting point:

```java
    public static String getFileContent(String fqn) throws IOException {
    String resourcePath = "/jsons/" + fqn + ".json"; // Move the "jsons" to a final variable

    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
```

## Example for MethodRegistry

When the project is compiled, a MethodRegistry class should've been generated, looking as follows:

```java
package org.wseresearch.processor;
import java.util.*;
import org.wseresearch.processor.MethodInfo;
public class MethodRegistry {private static final String METHOD_DOESNT_EXIST_ERROR_MESSAGE = "The requested method does not exist";
  public static List<MethodInfo> methods = 
    List.of(
      new MethodInfo("org.springframework.samples.petclinic.model.Person","getFirstName", "java.lang.String", "public String getFirstName() {      return this.firstName;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.model.Person","setFirstName", "void", "public void setFirstName(String firstName) {      this.firstName = firstName;  }", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.model.Person","getLastName", "java.lang.String", "public String getLastName() {      return this.lastName;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.model.Person","setLastName", "void", "public void setLastName(String lastName) {      this.lastName = lastName;  }", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","populatePetTypes", "java.util.Collection<org.springframework.samples.petclinic.owner.PetType>", "public Collection<PetType> populatePetTypes() {      return this.owners.findPetTypes();  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","findOwner", "org.springframework.samples.petclinic.owner.Owner", "public Owner findOwner(@PathVariable(\"ownerId\") int ownerId) {      Optional<Owner> optionalOwner = this.owners.findById(ownerId);      Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(\"Owner not found with id: \" + ownerId + \". Please ensure the ID is correct \"));      return owner;  }", List.of("int")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","findPet", "org.springframework.samples.petclinic.owner.Pet", "public Pet findPet(@PathVariable(\"ownerId\") int ownerId, @PathVariable(name = \"petId\", required = false) Integer petId) {      if (petId == null) {          return new Pet();      }      Optional<Owner> optionalOwner = this.owners.findById(ownerId);      Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(\"Owner not found with id: \" + ownerId + \". Please ensure the ID is correct \"));      return owner.getPet(petId);  }", List.of("int", "java.lang.Integer")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","initOwnerBinder", "void", "public void initOwnerBinder(WebDataBinder dataBinder) {      dataBinder.setDisallowedFields(\"id\");  }", List.of("org.springframework.web.bind.WebDataBinder")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","initPetBinder", "void", "public void initPetBinder(WebDataBinder dataBinder) {      dataBinder.setValidator(new PetValidator());  }", List.of("org.springframework.web.bind.WebDataBinder")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","initCreationForm", "java.lang.String", "public String initCreationForm(Owner owner, ModelMap model) {      Pet pet = new Pet();      owner.addPet(pet);      return VIEWS_PETS_CREATE_OR_UPDATE_FORM;  }", List.of("org.springframework.samples.petclinic.owner.Owner", "org.springframework.ui.ModelMap")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","processCreationForm", "java.lang.String", "public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, RedirectAttributes redirectAttributes) {      if (StringUtils.hasText(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null)          result.rejectValue(\"name\", \"duplicate\", \"already exists\");      LocalDate currentDate = LocalDate.now();      if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {          result.rejectValue(\"birthDate\", \"typeMismatch.birthDate\");      }      if (result.hasErrors()) {          return VIEWS_PETS_CREATE_OR_UPDATE_FORM;      }      owner.addPet(pet);      this.owners.save(owner);      redirectAttributes.addFlashAttribute(\"message\", \"New Pet has been Added\");      return \"redirect:/owners/{ownerId}\";  }", List.of("org.springframework.samples.petclinic.owner.Owner", "@jakarta.validation.Valid org.springframework.samples.petclinic.owner.Pet", "org.springframework.validation.BindingResult", "org.springframework.web.servlet.mvc.support.RedirectAttributes")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","initUpdateForm", "java.lang.String", "public String initUpdateForm() {      return VIEWS_PETS_CREATE_OR_UPDATE_FORM;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","processUpdateForm", "java.lang.String", "public String processUpdateForm(Owner owner, @Valid Pet pet, BindingResult result, RedirectAttributes redirectAttributes) {      String petName = pet.getName();      // checking if the pet name already exists for the owner      if (StringUtils.hasText(petName)) {          Pet existingPet = owner.getPet(petName, false);          if (existingPet != null && !existingPet.getId().equals(pet.getId())) {              result.rejectValue(\"name\", \"duplicate\", \"already exists\");          }      }      LocalDate currentDate = LocalDate.now();      if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {          result.rejectValue(\"birthDate\", \"typeMismatch.birthDate\");      }      if (result.hasErrors()) {          return VIEWS_PETS_CREATE_OR_UPDATE_FORM;      }      updatePetDetails(owner, pet);      redirectAttributes.addFlashAttribute(\"message\", \"Pet details has been edited\");      return \"redirect:/owners/{ownerId}\";  }", List.of("org.springframework.samples.petclinic.owner.Owner", "@jakarta.validation.Valid org.springframework.samples.petclinic.owner.Pet", "org.springframework.validation.BindingResult", "org.springframework.web.servlet.mvc.support.RedirectAttributes")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetController","updatePetDetails", "void", "private void updatePetDetails(Owner owner, Pet pet) {      Pet existingPet = owner.getPet(pet.getId());      if (existingPet != null) {          // Update existing pet's properties          existingPet.setName(pet.getName());          existingPet.setBirthDate(pet.getBirthDate());          existingPet.setType(pet.getType());      } else {          owner.addPet(pet);      }      this.owners.save(owner);  }", List.of("org.springframework.samples.petclinic.owner.Owner", "org.springframework.samples.petclinic.owner.Pet")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetTypeFormatter","print", "java.lang.String", "public String print(PetType petType, Locale locale) {      return petType.getName();  }", List.of("org.springframework.samples.petclinic.owner.PetType", "java.util.Locale")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetTypeFormatter","parse", "org.springframework.samples.petclinic.owner.PetType", "public PetType parse(String text, Locale locale) throws ParseException {      Collection<PetType> findPetTypes = this.owners.findPetTypes();      for (PetType type : findPetTypes) {          if (type.getName().equals(text)) {              return type;          }      }      throw new ParseException(\"type not found: \" + text, 0);  }", List.of("java.lang.String", "java.util.Locale")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetValidator","validate", "void", "public void validate(Object obj, Errors errors) {      Pet pet = (Pet) obj;      String name = pet.getName();      // name validation      if (!StringUtils.hasText(name)) {          errors.rejectValue(\"name\", REQUIRED, REQUIRED);      }      // type validation      if (pet.isNew() && pet.getType() == null) {          errors.rejectValue(\"type\", REQUIRED, REQUIRED);      }      // birth date validation      if (pet.getBirthDate() == null) {          errors.rejectValue(\"birthDate\", REQUIRED, REQUIRED);      }  }", List.of("java.lang.Object", "org.springframework.validation.Errors")),
      new MethodInfo("org.springframework.samples.petclinic.owner.PetValidator","supports", "boolean", "public boolean supports(Class<?> clazz) {      return Pet.class.isAssignableFrom(clazz);  }", List.of("java.lang.Class<?>")),
      new MethodInfo("org.springframework.samples.petclinic.owner.VisitController","setAllowedFields", "void", "public void setAllowedFields(WebDataBinder dataBinder) {      dataBinder.setDisallowedFields(\"id\");  }", List.of("org.springframework.web.bind.WebDataBinder")),
      new MethodInfo("org.springframework.samples.petclinic.owner.VisitController","loadPetWithVisit", "org.springframework.samples.petclinic.owner.Visit", "public Visit loadPetWithVisit(@PathVariable(\"ownerId\") int ownerId, @PathVariable(\"petId\") int petId, Map<String, Object> model) {      Optional<Owner> optionalOwner = owners.findById(ownerId);      Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(\"Owner not found with id: \" + ownerId + \". Please ensure the ID is correct \"));      Pet pet = owner.getPet(petId);      model.put(\"pet\", pet);      model.put(\"owner\", owner);      Visit visit = new Visit();      pet.addVisit(visit);      return visit;  }", List.of("int", "int", "java.util.Map<java.lang.String,java.lang.Object>")),
      new MethodInfo("org.springframework.samples.petclinic.owner.VisitController","initNewVisitForm", "java.lang.String", "public String initNewVisitForm() {      return \"pets/createOrUpdateVisitForm\";  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.VisitController","processNewVisitForm", "java.lang.String", "public String processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit, BindingResult result, RedirectAttributes redirectAttributes) {      if (result.hasErrors()) {          return \"pets/createOrUpdateVisitForm\";      }      owner.addVisit(petId, visit);      this.owners.save(owner);      redirectAttributes.addFlashAttribute(\"message\", \"Your visit has been booked\");      return \"redirect:/owners/{ownerId}\";  }", List.of("org.springframework.samples.petclinic.owner.Owner", "int", "@jakarta.validation.Valid org.springframework.samples.petclinic.owner.Visit", "org.springframework.validation.BindingResult", "org.springframework.web.servlet.mvc.support.RedirectAttributes")),
      new MethodInfo("org.springframework.samples.petclinic.system.CacheConfiguration","petclinicCacheConfigurationCustomizer", "org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer", "public JCacheManagerCustomizer petclinicCacheConfigurationCustomizer() {      return cm -> cm.createCache(\"vets\", cacheConfiguration());  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.system.CacheConfiguration","cacheConfiguration", "javax.cache.configuration.Configuration<java.lang.Object,java.lang.Object>", "private javax.cache.configuration.Configuration<Object, Object> cacheConfiguration() {      return new MutableConfiguration<>().setStatisticsEnabled(true);  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.vet.VetController","showVetList", "java.lang.String", "public String showVetList(@RequestParam(defaultValue = \"1\") int page, Model model) {      // Here we are returning an object of type 'Vets' rather than a collection of Vet      // objects so it is simpler for Object-Xml mapping      Vets vets = new Vets();      Page<Vet> paginated = findPaginated(page);      vets.getVetList().addAll(paginated.toList());      return addPaginationModel(page, paginated, model);  }", List.of("int", "org.springframework.ui.Model")),
      new MethodInfo("org.springframework.samples.petclinic.vet.VetController","addPaginationModel", "java.lang.String", "private String addPaginationModel(int page, Page<Vet> paginated, Model model) {      List<Vet> listVets = paginated.getContent();      model.addAttribute(\"currentPage\", page);      model.addAttribute(\"totalPages\", paginated.getTotalPages());      model.addAttribute(\"totalItems\", paginated.getTotalElements());      model.addAttribute(\"listVets\", listVets);      return \"vets/vetList\";  }", List.of("int", "org.springframework.data.domain.Page<org.springframework.samples.petclinic.vet.Vet>", "org.springframework.ui.Model")),
      new MethodInfo("org.springframework.samples.petclinic.vet.VetController","findPaginated", "org.springframework.data.domain.Page<org.springframework.samples.petclinic.vet.Vet>", "private Page<Vet> findPaginated(int page) {      int pageSize = 5;      Pageable pageable = PageRequest.of(page - 1, pageSize);      return vetRepository.findAll(pageable);  }", List.of("int")),
      new MethodInfo("org.springframework.samples.petclinic.vet.VetController","showResourcesVetList", "org.springframework.samples.petclinic.vet.Vets", "public Vets showResourcesVetList() {      // Here we are returning an object of type 'Vets' rather than a collection of Vet      // objects so it is simpler for JSon/Object mapping      Vets vets = new Vets();      vets.getVetList().addAll(this.vetRepository.findAll());      return vets;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.PetClinicApplication","main", "void", "public static void main(String[] args) {      SpringApplication.run(PetClinicApplication.class, args);  }", List.of("java.lang.String[]")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","setAllowedFields", "void", "public void setAllowedFields(WebDataBinder dataBinder) {      dataBinder.setDisallowedFields(\"id\");  }", List.of("org.springframework.web.bind.WebDataBinder")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","findOwner", "org.springframework.samples.petclinic.owner.Owner", "public Owner findOwner(@PathVariable(name = \"ownerId\", required = false) Integer ownerId) {      return ownerId == null ? new Owner() : this.owners.findById(ownerId).orElseThrow(() -> new IllegalArgumentException(\"Owner not found with id: \" + ownerId + \". Please ensure the ID is correct \" + \"and the owner exists in the database.\"));  }", List.of("java.lang.Integer")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","initCreationForm", "java.lang.String", "public String initCreationForm() {      return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","processCreationForm", "java.lang.String", "public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {      if (result.hasErrors()) {          redirectAttributes.addFlashAttribute(\"error\", \"There was an error in creating the owner.\");          return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;      }      this.owners.save(owner);      redirectAttributes.addFlashAttribute(\"message\", \"New Owner Created\");      return \"redirect:/owners/\" + owner.getId();  }", List.of("@jakarta.validation.Valid org.springframework.samples.petclinic.owner.Owner", "org.springframework.validation.BindingResult", "org.springframework.web.servlet.mvc.support.RedirectAttributes")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","initFindForm", "java.lang.String", "public String initFindForm() {      return \"owners/findOwners\";  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","processFindForm", "java.lang.String", "public String processFindForm(@RequestParam(defaultValue = \"1\") int page, Owner owner, BindingResult result, Model model) {      // allow parameterless GET request for /owners to return all records      if (owner.getLastName() == null) {          // empty string signifies broadest possible search          owner.setLastName(\"\");      }      // find owners by last name      Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, owner.getLastName());      if (ownersResults.isEmpty()) {          // no owners found          result.rejectValue(\"lastName\", \"notFound\", \"not found\");          return \"owners/findOwners\";      }      if (ownersResults.getTotalElements() == 1) {          // 1 owner found          owner = ownersResults.iterator().next();          return \"redirect:/owners/\" + owner.getId();      }      // multiple owners found      return addPaginationModel(page, model, ownersResults);  }", List.of("int", "org.springframework.samples.petclinic.owner.Owner", "org.springframework.validation.BindingResult", "org.springframework.ui.Model")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","addPaginationModel", "java.lang.String", "private String addPaginationModel(int page, Model model, Page<Owner> paginated) {      List<Owner> listOwners = paginated.getContent();      model.addAttribute(\"currentPage\", page);      model.addAttribute(\"totalPages\", paginated.getTotalPages());      model.addAttribute(\"totalItems\", paginated.getTotalElements());      model.addAttribute(\"listOwners\", listOwners);      return \"owners/ownersList\";  }", List.of("int", "org.springframework.ui.Model", "org.springframework.data.domain.Page<org.springframework.samples.petclinic.owner.Owner>")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","findPaginatedForOwnersLastName", "org.springframework.data.domain.Page<org.springframework.samples.petclinic.owner.Owner>", "private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {      int pageSize = 5;      Pageable pageable = PageRequest.of(page - 1, pageSize);      return owners.findByLastNameStartingWith(lastname, pageable);  }", List.of("int", "java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","initUpdateOwnerForm", "java.lang.String", "public String initUpdateOwnerForm() {      return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","processUpdateOwnerForm", "java.lang.String", "public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable(\"ownerId\") int ownerId, RedirectAttributes redirectAttributes) {      if (result.hasErrors()) {          redirectAttributes.addFlashAttribute(\"error\", \"There was an error in updating the owner.\");          return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;      }      if (owner.getId() != ownerId) {          result.rejectValue(\"id\", \"mismatch\", \"The owner ID in the form does not match the URL.\");          redirectAttributes.addFlashAttribute(\"error\", \"Owner ID mismatch. Please try again.\");          return \"redirect:/owners/{ownerId}/edit\";      }      owner.setId(ownerId);      this.owners.save(owner);      redirectAttributes.addFlashAttribute(\"message\", \"Owner Values Updated\");      return \"redirect:/owners/{ownerId}\";  }", List.of("@jakarta.validation.Valid org.springframework.samples.petclinic.owner.Owner", "org.springframework.validation.BindingResult", "int", "org.springframework.web.servlet.mvc.support.RedirectAttributes")),
      new MethodInfo("org.springframework.samples.petclinic.owner.OwnerController","showOwner", "org.springframework.web.servlet.ModelAndView", "public ModelAndView showOwner(@PathVariable(\"ownerId\") int ownerId) {      ModelAndView mav = new ModelAndView(\"owners/ownerDetails\");      Optional<Owner> optionalOwner = this.owners.findById(ownerId);      Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(\"Owner not found with id: \" + ownerId + \". Please ensure the ID is correct \"));      mav.addObject(owner);      return mav;  }", List.of("int")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Visit","getDate", "java.time.LocalDate", "public LocalDate getDate() {      return this.date;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Visit","setDate", "void", "public void setDate(LocalDate date) {      this.date = date;  }", List.of("java.time.LocalDate")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Visit","getDescription", "java.lang.String", "public String getDescription() {      return this.description;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Visit","setDescription", "void", "public void setDescription(String description) {      this.description = description;  }", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.PetClinicRuntimeHints","registerHints", "void", "public void registerHints(RuntimeHints hints, ClassLoader classLoader) {      // https://github.com/spring-projects/spring-boot/issues/32654      hints.resources().registerPattern(\"db/*\");      hints.resources().registerPattern(\"messages/*\");      hints.resources().registerPattern(\"mysql-default-conf\");      hints.serialization().registerType(BaseEntity.class);      hints.serialization().registerType(Person.class);      hints.serialization().registerType(Vet.class);  }", List.of("org.springframework.aot.hint.RuntimeHints", "java.lang.ClassLoader")),
      new MethodInfo("org.springframework.samples.petclinic.vet.Vets","getVetList", "java.util.List<org.springframework.samples.petclinic.vet.Vet>", "public List<Vet> getVetList() {      if (vets == null) {          vets = new ArrayList<>();      }      return vets;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","getAddress", "java.lang.String", "public String getAddress() {      return this.address;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","setAddress", "void", "public void setAddress(String address) {      this.address = address;  }", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","getCity", "java.lang.String", "public String getCity() {      return this.city;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","setCity", "void", "public void setCity(String city) {      this.city = city;  }", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","getTelephone", "java.lang.String", "public String getTelephone() {      return this.telephone;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","setTelephone", "void", "public void setTelephone(String telephone) {      this.telephone = telephone;  }", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","getPets", "java.util.List<org.springframework.samples.petclinic.owner.Pet>", "public List<Pet> getPets() {      return this.pets;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","addPet", "void", "public void addPet(Pet pet) {      if (pet.isNew()) {          getPets().add(pet);      }  }", List.of("org.springframework.samples.petclinic.owner.Pet")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","getPet", "org.springframework.samples.petclinic.owner.Pet", "Source code not found", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","getPet", "org.springframework.samples.petclinic.owner.Pet", "Source code not found", List.of("java.lang.Integer")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","getPet", "org.springframework.samples.petclinic.owner.Pet", "Source code not found", List.of("java.lang.String", "boolean")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","toString", "java.lang.String", "public String toString() {      return new ToStringCreator(this).append(\"id\", this.getId()).append(\"new\", this.isNew()).append(\"lastName\", this.getLastName()).append(\"firstName\", this.getFirstName()).append(\"address\", this.address).append(\"city\", this.city).append(\"telephone\", this.telephone).toString();  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Owner","addVisit", "void", "public void addVisit(Integer petId, Visit visit) {      Assert.notNull(petId, \"Pet identifier must not be null!\");      Assert.notNull(visit, \"Visit must not be null!\");      Pet pet = getPet(petId);      Assert.notNull(pet, \"Invalid Pet identifier!\");      pet.addVisit(visit);  }", List.of("java.lang.Integer", "org.springframework.samples.petclinic.owner.Visit")),
      new MethodInfo("org.springframework.samples.petclinic.system.WebConfiguration","localeResolver", "org.springframework.web.servlet.LocaleResolver", "public LocaleResolver localeResolver() {      SessionLocaleResolver resolver = new SessionLocaleResolver();      resolver.setDefaultLocale(Locale.ENGLISH);      return resolver;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.system.WebConfiguration","localeChangeInterceptor", "org.springframework.web.servlet.i18n.LocaleChangeInterceptor", "public LocaleChangeInterceptor localeChangeInterceptor() {      LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();      interceptor.setParamName(\"lang\");      return interceptor;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.system.WebConfiguration","addInterceptors", "void", "public void addInterceptors(InterceptorRegistry registry) {      registry.addInterceptor(localeChangeInterceptor());  }", List.of("org.springframework.web.servlet.config.annotation.InterceptorRegistry")),
      new MethodInfo("org.springframework.samples.petclinic.model.NamedEntity","getName", "java.lang.String", "public String getName() {      return this.name;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.model.NamedEntity","setName", "void", "public void setName(String name) {      this.name = name;  }", List.of("java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.model.NamedEntity","toString", "java.lang.String", "public String toString() {      return this.getName();  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.system.WelcomeController","welcome", "org.springframework.http.ResponseEntity<?>", "public ResponseEntity<?> welcome() {      return new ResponseEntity<>(MethodRegistry.getMethod(\"org.springframework.samples.petclinic.owner.Owner\", \"getCity\", List.of()).getSourceCode(), HttpStatus.OK);  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.system.WelcomeController","getMethodSourceCode", "java.lang.String", "public String getMethodSourceCode(@PathVariable String fqn, @PathVariable String method, @PathVariable String params) {      return MethodRegistry.getMethod(fqn, method, List.of(params.split(\",\"))).getSourceCode();  }", List.of("java.lang.String", "java.lang.String", "java.lang.String")),
      new MethodInfo("org.springframework.samples.petclinic.model.BaseEntity","getId", "java.lang.Integer", "public Integer getId() {      return id;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.model.BaseEntity","setId", "void", "public void setId(Integer id) {      this.id = id;  }", List.of("java.lang.Integer")),
      new MethodInfo("org.springframework.samples.petclinic.model.BaseEntity","isNew", "boolean", "public boolean isNew() {      return this.id == null;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Pet","setBirthDate", "void", "public void setBirthDate(LocalDate birthDate) {      this.birthDate = birthDate;  }", List.of("java.time.LocalDate")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Pet","getBirthDate", "java.time.LocalDate", "public LocalDate getBirthDate() {      return this.birthDate;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Pet","getType", "org.springframework.samples.petclinic.owner.PetType", "public PetType getType() {      return this.type;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Pet","setType", "void", "public void setType(PetType type) {      this.type = type;  }", List.of("org.springframework.samples.petclinic.owner.PetType")),
      new MethodInfo("org.springframework.samples.petclinic.owner.Pet","getVisits", "java.util.Collection<org.springframework.samples.petclinic.owner.Visit>", "public Collection<Visit> getVisits() {      return this.visits;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.owner.Pet","addVisit", "void", "public void addVisit(Visit visit) {      getVisits().add(visit);  }", List.of("org.springframework.samples.petclinic.owner.Visit")),
      new MethodInfo("org.springframework.samples.petclinic.vet.Vet","getSpecialtiesInternal", "java.util.Set<org.springframework.samples.petclinic.vet.Specialty>", "protected Set<Specialty> getSpecialtiesInternal() {      if (this.specialties == null) {          this.specialties = new HashSet<>();      }      return this.specialties;  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.vet.Vet","getSpecialties", "java.util.List<org.springframework.samples.petclinic.vet.Specialty>", "public List<Specialty> getSpecialties() {      return getSpecialtiesInternal().stream().sorted(Comparator.comparing(NamedEntity::getName)).collect(Collectors.toList());  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.vet.Vet","getNrOfSpecialties", "int", "public int getNrOfSpecialties() {      return getSpecialtiesInternal().size();  }", List.of()),
      new MethodInfo("org.springframework.samples.petclinic.vet.Vet","addSpecialty", "void", "public void addSpecialty(Specialty specialty) {      getSpecialtiesInternal().add(specialty);  }", List.of("org.springframework.samples.petclinic.vet.Specialty")),
      new MethodInfo("org.springframework.samples.petclinic.system.CrashController","triggerException", "java.lang.String", "public String triggerException() {      throw new RuntimeException(\"Expected: controller used to showcase what \" + \"happens when an exception is thrown\");  }", List.of())
);
public static MethodInfo getMethod(String fqn, String methodName, List<String> parameterTypes) {
   for (MethodInfo method : methods) {
      if (method.fqn.equals(fqn) && method.methodName.equals(methodName)) {
        List<String> paramTypes = method.getParameterTypes();
        if (paramTypes.equals(parameterTypes)) {
            return method;
        } else continue;
     }
  }
  throw new NoSuchMethodError(METHOD_DOESNT_EXIST_ERROR_MESSAGE);
  }
}

```
