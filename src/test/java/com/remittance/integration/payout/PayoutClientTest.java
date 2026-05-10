package com.remittance.integration.payout;

import com.remittance.integration.payout.dto.TransferRecipientRequest;
import com.remittance.integration.payout.dto.TransferRecipientResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = {
                PayoutClient.class,
                PayoutClientTest.TestConfig.class
        },
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",

                // Replace with your real Paystack key
                "paystack.secret.key=sk_test_xxxxxxxxxxxxxxxxx",

                "paystack.base-url=https://api.paystack.co"
        }
)
@Import(PayoutClientTest.TestConfig.class)
class PayoutClientTest {

    @Autowired
    private PayoutClient payoutClient;

    @Test
    void shouldCreateTransferRecipientSuccessfully() {

        TransferRecipientRequest request =
                new TransferRecipientRequest(
                        "nuban",
                        "Integration Test User",
                        "0069087573",
                        "044",
                        "NGN"
                );

        TransferRecipientResponse response =
                payoutClient.createTransferRecipient(request);

        assertNotNull(response);
        assertTrue(response.status());
        assertNotNull(response.data());
        assertNotNull(response.data().recipientCode());

        System.out.println(
                "Recipient Code: "
                        + response.data().recipientCode()
        );
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        RestClient restClient(
                @Value("${paystack.base-url}") String baseUrl,
                @Value("${paystack.secret.key}") String secretKey
        ) {

            return RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(
                            "Authorization",
                            "Bearer " + secretKey
                    )
                    .defaultHeader(
                            "Content-Type",
                            "application/json"
                    )
                    .build();
        }
    }
}
