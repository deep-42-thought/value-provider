package com.tngtech.valueprovider;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.UseDataProvider;
import com.tngtech.junit.dataprovider.UseDataProviderExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.junit.dataprovider.DataProviders.$;
import static com.tngtech.junit.dataprovider.DataProviders.$$;
import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// sequence of extensions does NOT seem to matter
@ExtendWith({ValueProviderExtension.class, UseDataProviderExtension.class})
public class ClassLifecycleDemoTest {
    private static final Logger logger = LoggerFactory.getLogger(ClassLifecycleDemoTest.class);

    // as execution sequence of tests may vary
    private static final List<ValueProvider> allRandoms = new ArrayList<>();
    private static ValueProvider beforeAllRandom;

    private ValueProvider instanceRandom = createRandomValueProvider();

    @BeforeAll
    static void beforeAll() {
        ensureDefinedFactoryState();
        beforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void beforeEach() {
        allRandoms.add(createRandomValueProvider());
    }

    @DataProvider
    public static Object[][] testValues1() {
        logger.debug("create DataProvider 1");
        ValueProvider dataProviderRandom = createRandomValueProvider();
        allRandoms.add(dataProviderRandom);
        return $$(
                $(dataProviderRandom.fixedDecoratedString("1")),
                $(dataProviderRandom.fixedDecoratedString("2"))
        );
    }

    @TestTemplate
    @UseDataProvider("testValues1")
    void should_ensure_reproducible_ValueProvider_creation_for_DataProvider(String testValue) {
        assertThat(testValue).isNotEmpty();
        verifyReproducibleValueProviderCreation();
    }

    @DataProvider
    public static Object[][] testValues2() {
        logger.debug("create DataProvider 2");
        ValueProvider dataProviderRandom = createRandomValueProvider();
        allRandoms.add(dataProviderRandom);
        return $$(
                $(dataProviderRandom.fixedDecoratedString("1")),
                $(dataProviderRandom.fixedDecoratedString("2"))
        );
    }

    @TestTemplate
    @UseDataProvider("testValues2")
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles_for_DataProvider(String testValue) {
        assertThat(testValue).isNotEmpty();
        verifyReproducibleValueProviderCreation();
    }

    @Test
    void should_ensure_reproducible_ValueProvider_creation() {
        verifyReproducibleValueProviderCreation();
    }

    @Test
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    @AfterAll
    void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(beforeAllRandom)
                .addExpectedTestClassRandomValues(allRandoms)
                .assertAllTestClassRandomValues()
                .assertAllSuffixes();
    }
}
