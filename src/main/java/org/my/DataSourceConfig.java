package org.my;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class DataSourceConfig {

    @Value("${spring.cloud.aws.secretsmanager.name}")
    private String secretName;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.url}")
    private String url;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DatabaseProperties{
        private String username;
        private String password;
    }

    @Bean
    @Profile({"dev","stg","prod"})
    public DataSource getDataSource() throws IOException {

        return getDataSource(getDatabasePropertiesFromAwsSecrets(secretName, region));
    }

    private DataSource getDataSource(DatabaseProperties databaseProperties){
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(driverClassName);

        dataSourceBuilder.url(url);
        dataSourceBuilder.username(databaseProperties.getUsername());
        dataSourceBuilder.password(databaseProperties.getPassword());

        return dataSourceBuilder.build();
    }

    public static DatabaseProperties getDatabasePropertiesFromAwsSecrets(String secretName, String region) throws IOException {
        // Create a Secrets Manager client
        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        String secret = null, decodedBinarySecret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException| InternalServiceErrorException |InvalidParameterException|InvalidRequestException |ResourceNotFoundException e ) {
            throw e;
        }
        // Decrypts secret using the associated KMS key.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
        }
        else {
            decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
        ObjectMapper mapper = new ObjectMapper();

        DatabaseProperties databaseProperties = mapper.readValue(secret, DatabaseProperties.class);
        return  databaseProperties;
    }
}
