package dev.ikm.tinkar.rxnorm.integration;

import dev.ikm.maven.RxnormData;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RxnormDescriptionSemanticIT extends AbstractIntegrationTest {

    /**
     * Test RxnormDescriptions Semantics.
     *
     * @result Reads content from file and validates Description of Semantics by calling private method assertOwlElement().
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
            UUID rxnormUuid = conceptUuid(rxnormData.getId());
            EntityProxy.Concept concept = EntityProxy.Concept.make(PublicIds.of(rxnormUuid));
            StateSet stateActive = StateSet.ACTIVE;
            StampCalculator stampCalcActive = StampCalculatorWithCache
                    .getCalculator(StampCoordinateRecord.make(stateActive, Coordinates.Position.LatestOnDevelopment()));
            PatternEntityVersion latestDescriptionPattern = (PatternEntityVersion) Calculators.Stamp.DevelopmentLatest().latest(TinkarTerm.DESCRIPTION_PATTERN).get();
            AtomicBoolean rxnormName = new AtomicBoolean(!rxnormData.getRxnormName().isEmpty());
            AtomicBoolean rxnormSynonym = new AtomicBoolean(!rxnormData.getRxnormSynonym().isEmpty());
            AtomicBoolean rxnormPrescribableSynonym = new AtomicBoolean(!rxnormData.getPrescribableSynonym().isEmpty());
            AtomicReference<List<String>> rxnormTallmanSynonym = new AtomicReference<>();

            if (!rxnormData.getTallmanSynonyms().isEmpty()) {
                rxnormTallmanSynonym.set(rxnormData.getTallmanSynonyms());
            } else {
                rxnormTallmanSynonym.set(new ArrayList<>());
            }

            AtomicBoolean matchedName = new AtomicBoolean(!rxnormName.get());
            AtomicBoolean matchedSynonym = new AtomicBoolean(!rxnormSynonym.get());
            AtomicBoolean matchedPrescribableSynonym = new AtomicBoolean(!rxnormPrescribableSynonym.get());
            AtomicInteger matchedTallmanSynonyms = new AtomicInteger(0);

            AtomicInteger counter = new AtomicInteger(0);

            EntityService.get().forEachSemanticForComponentOfPattern(concept.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(), semanticEntity -> {
                Latest<SemanticEntityVersion> latestActive = stampCalcActive.latest(semanticEntity);

                counter.incrementAndGet();
                if (latestActive.isPresent()) {
                    String textForDesc = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.TEXT_FOR_DESCRIPTION, latestActive.get());
                    Component descCaseSignificance = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE, latestActive.get());
                    Component descType = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_TYPE, latestActive.get());

                    if (rxnormName.get()) {
                        if (rxnormData.getId().equals("1000001")) {
                            LOG.info("***JTD: rxnormName " + rxnormData.getRxnormName() + ", " + textForDesc + ": "+textForDesc.equals(rxnormData.getRxnormName()));
                            LOG.info("***JTD: rxnormName desc case " + DESCRIPTION_NOT_CASE_SENSITIVE + ", " + descCaseSignificance + ": " + descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE));
                            LOG.info("***JTD: rxnormName desc type " + FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE + ", " + descType + ": "+(descType.equals(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)));
                        }
                        if (textForDesc.equals(rxnormData.getRxnormName())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && descType.equals(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)) {
                            matchedName.set(true);
                            rxnormName.set(false);
                        }
                    }

                    if (rxnormSynonym.get()) {
                        if (rxnormData.getId().equals("1000001")) {
                            LOG.info("***JTD: rxnormSynonym " + rxnormData.getRxnormSynonym() + ", " + textForDesc + ": "+textForDesc.equals(rxnormData.getRxnormSynonym()));
                            LOG.info("***JTD: rxnormSynonym desc case " + DESCRIPTION_NOT_CASE_SENSITIVE + ", " + descCaseSignificance + ": " + descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE));
                            LOG.info("***JTD: rxnormSynonym desc type " + REGULAR_NAME_DESCRIPTION_TYPE + ", " + descType + ": "+(descType.equals(REGULAR_NAME_DESCRIPTION_TYPE)));
                        }

                        if (textForDesc.equals(rxnormData.getRxnormSynonym())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && (descType.equals(REGULAR_NAME_DESCRIPTION_TYPE))) {
                            matchedSynonym.set(true);
                            rxnormSynonym.set(false);
                        }
                    }

                    if (rxnormPrescribableSynonym.get()) {
                        if (rxnormData.getId().equals("1000001")) {
                            LOG.info("***JTD: rxnormPrescribableSynonym " + rxnormData.getRxnormSynonym() + ", " + textForDesc + ": "+textForDesc.equals(rxnormData.getPrescribableSynonym()));
                            LOG.info("***JTD: rxnormPrescribableSynonym desc case " + DESCRIPTION_NOT_CASE_SENSITIVE + ", " + descCaseSignificance + ": " + descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE));
                            LOG.info("***JTD: rxnormPrescribableSynonym desc type " + REGULAR_NAME_DESCRIPTION_TYPE + ", " + descType + ": "+(descType.equals(REGULAR_NAME_DESCRIPTION_TYPE)));
                        }
                        if (textForDesc.equals(rxnormData.getPrescribableSynonym())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && (descType.equals(REGULAR_NAME_DESCRIPTION_TYPE))) {
                            matchedPrescribableSynonym.set(true);
                            rxnormPrescribableSynonym.set(false);
                        }
                    }

                    List<String> synonyms = rxnormTallmanSynonym.get();
                    if (!synonyms.isEmpty()) {
                        synonyms.forEach(synonym -> {
                            if (rxnormData.getId().equals("1000001")) {
                                LOG.info("***JTD: rxnormTallmanSynonym " + rxnormData.getRxnormSynonym() + ", " + textForDesc + ": "+textForDesc.equals(synonym));
                                LOG.info("***JTD: rxnormTallmanSynonym desc case " + DESCRIPTION_CASE_SENSITIVE + ", " + descCaseSignificance + ": " + descCaseSignificance.equals(DESCRIPTION_CASE_SENSITIVE));
                                LOG.info("***JTD: rxnormTallmanSynonym desc type " + REGULAR_NAME_DESCRIPTION_TYPE + ", " + descType + ": "+(descType.equals(REGULAR_NAME_DESCRIPTION_TYPE)));
                            }
                            if (textForDesc.equals(synonym)
                                    && descCaseSignificance.equals(DESCRIPTION_CASE_SENSITIVE)
                                    && descType.equals(REGULAR_NAME_DESCRIPTION_TYPE)) {
                                matchedTallmanSynonyms.incrementAndGet();
                                if (rxnormData.getId().equals("1000001")) {
                                    LOG.info("***JTD: matched " + matchedTallmanSynonyms.get());
                                }
                            } else  if (rxnormData.getId().equals("1000001")) {
                                LOG.info("***JTD: no match " +textForDesc.equals(synonym) +", "+descCaseSignificance.equals(DESCRIPTION_CASE_SIGNIFICANCE)+", "+descType.equals(REGULAR_NAME_DESCRIPTION_TYPE));
                            }
                        });
                    }
                }
                if (rxnormData.getId().equals("1000001")) {
                    LOG.info("***JTD: count is " + counter.get());
                }
            });

            if (RxnormDescriptionSemanticIT.count < 1) {
                if (!(matchedName.get() && matchedSynonym.get() && matchedPrescribableSynonym.get() && matchedTallmanSynonyms.get() == rxnormTallmanSynonym.get().size())) {
                    LOG.info("***JTD: matchedName.get(): " + matchedName.get() + " - matchedSynonym.get(): " + matchedSynonym.get() + " - matchedPrescribableSynonym.get(): " + matchedPrescribableSynonym.get() + " - matchedTallman " + matchedTallmanSynonyms.get() + " =? " + rxnormTallmanSynonym.get().size());
                    LOG.info("***JTD: concept " + concept + ", concept publicId " + concept.publicId());
                    LOG.info("***JTD: id " + rxnormData.getId() + " name " + rxnormData.getRxnormName() + ", synonym " + rxnormData.getRxnormSynonym() + ", prescribable "+rxnormData.getPrescribableSynonym());
                    EntityProxy.Semantic semantic = EntityProxy.Semantic.make(
                            PublicIds.of(UuidT5Generator.get(UUID.fromString(namespaceString), concept.publicId().asUuidArray()[0] + rxnormData.getRxnormName() + "DESC")));
                    LOG.info("***JTD: semantic for name " + semantic);
                    Entity<SemanticEntityVersion> e = EntityService.get().getEntityFast(UuidT5Generator.get(UUID.fromString(namespaceString), concept.publicId().asUuidArray()[0] + rxnormData.getRxnormName() + "DESC"));
                    if (e == null) {
                        LOG.error("***JTD: semantic entity not found");
                    } else {
                        LOG.info("***JTD: semantic entity found "+e);
                        LOG.info("***JTD: latest active " + stampCalcActive.latest(e));
                        LOG.info("***JTD: reference: " +stampCalcActive.latest(e).get().referencedComponent());
                        LOG.info("***JTD: concept: " + stampCalcActive.latest(concept));
                        LOG.info("***JTD: pattern: " + stampCalcActive.latest(e).get().pattern());
                    }
                    LOG.info("***JTD: trying to find  semantic with " + namespaceString + ", " + concept.publicId().asUuidArray()[0] + rxnormData.getRxnormName() + "DESC");
                    LOG.info("***JTD: pattern " + TinkarTerm.DESCRIPTION_PATTERN);

                    RxnormDescriptionSemanticIT.count++;
                }
            }
            return (matchedName.get() && matchedSynonym.get() && matchedPrescribableSynonym.get() && matchedTallmanSynonyms.get() == rxnormTallmanSynonym.get().size());
        }
        return false; // TOTAL 28
    }
    static int count = 0;
}
