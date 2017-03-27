package com.rest;

import com.db.jpa.Person;
import com.db.jpa.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;

/**
 * @author Chris.Ge
 */
@RestController
@RequestMapping("/people/{id}/photo")
public class PersonPhotoRestController {

    private File root;

    @Autowired
    private PersonRepository personRepository;

    @Value("${user.home}")
    public void setUserHome(String home) {
        this.root = new File(home, "Desktop/images");
        Assert.isTrue(this.root.exists() || this.root.mkdirs(),
            "The path '" + this.root.getAbsolutePath() + "' must exist.");
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Resource> read(@PathVariable Long id) throws Exception {
        Person person = findOne(id);
        File file = fileFor(person);
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        //or use @
        httpHeaders.setContentType(MediaType.IMAGE_PNG);
        // inputstreamresource - FileInputStream
        Resource resource = new FileSystemResource(file);
        return new ResponseEntity<Resource>(resource, httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<?> write(@PathVariable Long id, @RequestParam MultipartFile file)
        throws Exception {
        Person person = this.personRepository.findOne(id);
        FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(fileFor(person)));

        // expand method replaces query parameter /people/{id}/photo {id} with id

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    private File fileFor(Person person) {
        return new File(this.root, Long.toString(person.getId()));
    }

    private Person findOne(Long id) {
        Person person = this.personRepository.findOne(id);
        if (person == null)
            throw new PersonNotFoundException(id);
        return person;
    }

}
