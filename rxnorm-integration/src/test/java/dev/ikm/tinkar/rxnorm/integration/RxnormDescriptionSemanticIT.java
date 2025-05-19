package dev.ikm.tinkar.rxnorm.integration;

import dev.ikm.maven.RxnormData;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RxnormDescriptionSemanticIT extends AbstractIntegrationTest {

    /**
     * Test RxnormDescriptions Semantics.
     *
     * @result Reads content from file and validates Description of Semantics by calling private method assertConcept().
     */
    @Test
    public void testRxnormDescriptionSemantics() throws IOException {
        String sourceFilePath = "../rxnorm-origin/";
        String errorFile = "target/failsafe-reports/Rxnorm_Descriptions_not_found.txt";
        String absolutePath = findFilePath(sourceFilePath, rxnormOwlFileName);
        int notFound = processOwlFile(absolutePath, errorFile);

        assertEquals(0, notFound, "Unable to find " + notFound + " Rxnorm Description semantics. Details written to " + errorFile);
    }

    @Override
    protected boolean assertOwlElement(RxnormData rxnormData) {
        if (rxnormData.getId() != null) {
            // Generate UUID based on RxNorm ID
            UUID conceptUuid = uuid(rxnormData.getId());

//            EntityProxy.Concept concept = EntityProxy.Concept.make(PublicIds.of(conceptUuid));
            EntityProxy.Semantic descSemantic =
                    EntityProxy.Semantic.make(PublicIds.of(UuidT5Generator.get(UUID.fromString(namespaceString),
                    (EntityProxy.Concept.make(PublicIds.of(conceptUuid))).publicId().asUuidArray()[0] + rxnormData.getRxnormName() + "DESC")));
//            (concept.publicId().asUuidArray()[0] + rxnormData.getRxnormName() + "DESC")

            StateSet state = StateSet.ACTIVE;
            StampPositionRecord stampPosition = StampPositionRecord.make(timeForStamp, TinkarTerm.DEVELOPMENT_PATH.nid());
            StampCalculator stampCalc = StampCoordinateRecord.make(state, stampPosition).stampCalculator();
//            ConceptRecord entity = EntityService.get().getEntityFast(conceptUuid);
//            SemanticRecord entity = EntityService.get().getEntity();
//            SemanticRecord entity = EntityService.get().getEntityFast(conceptUuid);
            SemanticRecord entity = EntityService.get().getEntityFast(descSemantic.uuids());

            PatternEntityVersion latestDescriptionPattern = (PatternEntityVersion) Calculators.Stamp.DevelopmentLatest().latest(TinkarTerm.DESCRIPTION_PATTERN).get();
//            Latest<ConceptVersionRecord> latest = stampCalc.latest(entity);
            Latest<SemanticVersionRecord> latest = stampCalc.latest(entity);

            if (latest.isPresent()) {
                System.out.println("latest.isPresent(): " + latest.get().toString());

                String textForDesc = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.TEXT_FOR_DESCRIPTION, latest.get());
                Component descCaseSignificance = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE, latest.get());
                Component descType = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_TYPE, latest.get());

                return ((textForDesc.equals(rxnormData.getRxnormName())
                                ||  textForDesc.equals(rxnormData.getRxnormSynonym())
                                || textForDesc.equals(rxnormData.getPrescribableSynonym()))
                        && (descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE))
                        && (descType.equals(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE) || descType.equals(REGULAR_NAME_DESCRIPTION_TYPE))
                );
            }
        }
        return false;
    }

}
