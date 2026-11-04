package com.workshop.loanservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Compares each read endpoint against a JSON snapshot in src/test/resources/golden/. Snapshots
 * record current behaviour; a diff means behaviour changed.
 */
@SpringBootTest(properties = {
        "loan.exposure.simulated-latency-ms=0",
        "spring.datasource.url=jdbc:h2:mem:golden;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@AutoConfigureMockMvc
class GoldenSnapshotTest {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest(name = "{0} matches golden/{1}")
    @CsvSource({
            "/api/loans, loans.json",
            "/api/loans/LN-2019-00142, loan-LN-2019-00142.json",
            "/api/loans/LN-2018-00089/payments, payments-LN-2018-00089.json",
            "/api/loans/LN-0000-00000/payments, payments-unknown-loan.json",
            "/api/borrowers, borrowers.json",
            "/api/borrowers/B-10002, borrower-B-10002.json",
            "/api/borrowers/B-10001/exposure, exposure-B-10001.json",
            "/api/borrowers/B-10003/exposure, exposure-B-10003.json",
            "/api/borrowers/B-10005/exposure, exposure-B-10005.json"
    })
    void responseMatchesGoldenSnapshot(String path, String goldenFile) throws Exception {
        String actual = mvc.perform(get(path))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expected = new ClassPathResource("golden/" + goldenFile)
                .getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }
}
