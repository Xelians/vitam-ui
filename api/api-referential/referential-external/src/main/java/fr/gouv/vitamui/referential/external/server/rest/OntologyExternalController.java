/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.external.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.referential.common.dto.OntologyDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.OntologyExternalService;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.ONTOLOGIES_URL)
@Getter
@Setter
public class OntologyExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OntologyExternalController.class);

    @Autowired
    private OntologyExternalService ontologyExternalService;

    @GetMapping()
    @Secured(ServicesData.ROLE_GET_ONTOLOGIES)
    public Collection<OntologyDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all ontology criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return ontologyExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_ONTOLOGIES)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<OntologyDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        return ontologyExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public OntologyDto getOne(final @PathVariable("identifier") String identifier) {
        LOGGER.debug("get file format identifier={}");
        return ontologyExternalService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_ONTOLOGIES })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody OntologyDto ontologyDto, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("check exist ontology={}", ontologyDto);
        final boolean exist = ontologyExternalService.check(ontologyDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_AGENCIES)
    public OntologyDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return ontologyExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_CREATE_ONTOLOGIES)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OntologyDto create(final @Valid @RequestBody OntologyDto accessContractDto) {
        LOGGER.debug("Create {}", accessContractDto);
        return ontologyExternalService.create(accessContractDto);
    }

    @Secured(ServicesData.ROLE_GET_ONTOLOGIES)
    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        return ontologyExternalService.findHistoryById(id);
    }

    @Secured(ServicesData.ROLE_DELETE_ONTOLOGIES)
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable("id") String id) {
        LOGGER.debug("Delete ontology with id :{}", id);
        ontologyExternalService.delete(id);
    }
}