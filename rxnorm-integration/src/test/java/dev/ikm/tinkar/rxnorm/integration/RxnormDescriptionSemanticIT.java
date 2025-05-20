package dev.ikm.tinkar.rxnorm.integration;

import dev.ikm.maven.RxnormData;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @Disabled // TODO
    public void testRxnormDescriptionSemantics() throws IOException {
        String errorFile = "target/failsafe-reports/Rxnorm_Descriptions_not_found.txt";
        String absolutePath = rxnormOwlFileName;
        int notFound = processOwlFile(absolutePath, errorFile);

        assertEquals(0, notFound, "Unable to find " + notFound + " Rxnorm Description semantics. Details written to " + errorFile);
    }

    @Override
    protected boolean assertOwlElement(RxnormData rxnormData) {
        if (rxnormData.getId() != null) {
            // Generate UUID based on RxNorm ID
            UUID conceptUuid = uuid(rxnormData.getId());
            EntityProxy.Concept concept = EntityProxy.Concept.make(PublicIds.of(conceptUuid));
            StateSet state = StateSet.ACTIVE;
            StampPositionRecord stampPosition = StampPositionRecord.make(timeForStamp, TinkarTerm.DEVELOPMENT_PATH.nid());
            StampCalculator stampCalc = StampCoordinateRecord.make(state, stampPosition).stampCalculator();
            PatternEntityVersion latestDescriptionPattern = (PatternEntityVersion) Calculators.Stamp.DevelopmentLatest().latest(TinkarTerm.DESCRIPTION_PATTERN).get();
            AtomicBoolean matched = new AtomicBoolean(true);

            EntityService.get().forEachSemanticForComponentOfPattern(concept.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(), semanticEntity -> {
                Latest<SemanticEntityVersion> latest = stampCalc.latest(semanticEntity);

                if (latest.isPresent()) {
                    String textForDesc = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.TEXT_FOR_DESCRIPTION, latest.get());
                    Component descCaseSignificance = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE, latest.get());
                    Component descType = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_TYPE, latest.get());

                    if (!rxnormData.getRxnormName().isEmpty()) {
                        if (textForDesc.equals(rxnormData.getRxnormName())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && descType.equals(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)) {
                            matched.set(true);
                        }
                    } else if (!rxnormData.getRxnormSynonym().isEmpty()) {
                        if (textForDesc.equals(rxnormData.getRxnormSynonym())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && descType.equals(REGULAR_NAME_DESCRIPTION_TYPE)) {
                            matched.set(true);
                        }
                    } else if (!rxnormData.getPrescribableSynonym().isEmpty()) {
                        if (textForDesc.equals(rxnormData.getPrescribableSynonym())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && descType.equals(REGULAR_NAME_DESCRIPTION_TYPE)) {
                            matched.set(true);
                        }
                    } else {
                        matched.set(false);
                    }
                } else {
                    matched.set(false);
                }

            });

            return matched.get();
        }
//        System.out.println("rxnormData.getId() == null: "); // TOTAL 28
        return false;
    }

}
