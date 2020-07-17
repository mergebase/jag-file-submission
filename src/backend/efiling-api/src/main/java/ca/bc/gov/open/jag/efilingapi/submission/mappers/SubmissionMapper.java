package ca.bc.gov.open.jag.efilingapi.submission.mappers;

import ca.bc.gov.open.jag.efilingapi.api.model.GenerateUrlRequest;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import ca.bc.gov.open.jag.efilingcommons.model.AccountDetails;
import ca.bc.gov.open.jag.efilingcommons.model.ServiceFees;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SubmissionMapper {

    @Mapping(source = "submissionId", target = "id")
    @Mapping(source = "owner", target = "owner")
    @Mapping(source = "generateUrlRequest.navigation", target = "navigation")
    @Mapping(source = "generateUrlRequest.clientApplication", target = "clientApplication")
    @Mapping(source = "generateUrlRequest.filingPackage", target = "filingPackage")
    @Mapping(source = "fees", target = "fees")
    @Mapping(source = "accountDetails", target = "accountDetails")
    @Mapping(source = "expiryDate", target = "expiryDate")
    Submission toSubmission(UUID submissionId, UUID owner, GenerateUrlRequest generateUrlRequest, List<ServiceFees> fees, AccountDetails accountDetails, long expiryDate);

}
